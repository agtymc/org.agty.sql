package org.agty.sql.pool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.agty.sql.AgtySQL;
import org.agty.sql.config.AgtySqlConfig;
import org.agty.sql.connect.AgtySqlConnector;
import org.agty.sql.datasource.AgtySqlDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Compatibility adapter exposing pooled {@link AgtySQL} sessions backed by HikariCP.
 *
 * <p>The pool is thread-safe. Each borrowed {@link PooledAgtySQL} handle is
 * stateful, is not thread-safe, and must be closed by its borrower.</p>
 */
public final class AgtySQLPool implements AutoCloseable {
    private static final AtomicLong POOL_SEQUENCE = new AtomicLong();
    private static final long HIKARI_MIN_CONNECTION_TIMEOUT_MILLIS = 250L;
    private static final long HIKARI_MIN_MAX_LIFETIME_MILLIS = 30_000L;

    private final AgtySqlConfig config;
    private final int maxPoolSize;
    private final Duration defaultBorrowTimeout;
    private final HikariDataSource dataSource;
    private final Semaphore permits;
    private final Set<PooledAgtySQL> activeLeases = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public AgtySQLPool(
            AgtySqlConfig config,
            int maxPoolSize,
            Duration maxLifetime,
            Duration defaultBorrowTimeout
    ) {
        if (config == null) {
            throw new IllegalArgumentException("AgtySqlConfig must not be null");
        }
        if (maxPoolSize < 1) {
            throw new IllegalArgumentException("maxPoolSize must be greater than zero");
        }
        requirePositive(maxLifetime, "maxLifetime");
        requirePositive(defaultBorrowTimeout, "defaultBorrowTimeout");

        this.config = AgtySqlConfig.getClone(config);
        this.maxPoolSize = maxPoolSize;
        this.defaultBorrowTimeout = defaultBorrowTimeout;
        this.permits = new Semaphore(maxPoolSize, true);
        this.dataSource = new HikariDataSource(createHikariConfig(maxLifetime));
    }

    public AgtySQLPool(AgtySqlConfig config, int maxPoolSize, Duration maxLifetime) {
        this(config, maxPoolSize, maxLifetime, Duration.ofMillis(300));
    }

    public AgtySQLPool(AgtySqlConfig config, int maxPoolSize) {
        this(config, maxPoolSize, 30);
    }

    public AgtySQLPool(AgtySqlConfig config, int maxPoolSize, int durationMinutes) {
        this(config, maxPoolSize, Duration.ofMinutes(durationMinutes));
    }

    public PooledAgtySQL borrow() throws SQLException {
        return borrow(defaultBorrowTimeout);
    }

    public PooledAgtySQL borrow(Duration timeout) throws SQLException {
        ensureOpen();
        Duration effectiveTimeout = timeout == null || timeout.isZero() || timeout.isNegative()
                ? Duration.ofMillis(1)
                : timeout;

        boolean acquired;
        try {
            acquired = permits.tryAcquire(effectiveTimeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for a DB connection", exception);
        }
        if (!acquired) {
            throw new SQLException("Connection timeout");
        }

        if (closed.get()) {
            permits.release();
            throw new IllegalStateException("Pool closed");
        }

        Connection connection = null;
        boolean registered = false;
        try {
            connection = dataSource.getConnection();
            PooledAgtySQL lease = new PooledAgtySQL(
                    new AgtySQL(new AgtySqlConnector(config, guard(connection))),
                    this
            );
            activeLeases.add(lease);
            registered = true;
            if (closed.get()) {
                lease.closeFromPool();
                throw new IllegalStateException("Pool closed");
            }
            return lease;
        } catch (RuntimeException | SQLException exception) {
            if (!registered) {
                if (connection != null) {
                    closeAfterFailedBorrow(connection, exception);
                }
                permits.release();
            }
            throw exception;
        }
    }

    public PooledAgtySQL borrowExtended(Duration timeout) throws SQLException {
        PooledAgtySQL lease = borrow(timeout == null ? defaultBorrowTimeout : timeout);
        if (lease.isHealthy()) {
            return lease;
        }
        lease.close();
        throw new SQLException("Borrowed connection is not healthy");
    }

    /** Creates and validates up to {@code count} physical connections eagerly. */
    public void preload(int count) {
        ensureOpen();
        int target = Math.max(0, Math.min(count, maxPoolSize));
        List<Connection> connections = new ArrayList<>(target);
        try {
            for (int index = 0; index < target; index++) {
                connections.add(dataSource.getConnection());
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to preload DB connections", exception);
        } finally {
            for (Connection connection : connections) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                    // Hikari evicts a connection that cannot be returned cleanly.
                }
            }
        }
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        for (PooledAgtySQL lease : List.copyOf(activeLeases)) {
            lease.closeFromPool();
        }
        dataSource.close();
    }

    private HikariConfig createHikariConfig(Duration maxLifetime) {
        HikariConfig hikari = new HikariConfig();
        hikari.setPoolName("agty-sql-" + POOL_SEQUENCE.incrementAndGet());
        hikari.setDataSource(new AgtySqlDataSource(config));
        hikari.setMaximumPoolSize(maxPoolSize);
        hikari.setMinimumIdle(0);
        hikari.setAutoCommit(config.isAutoCommit());
        if (config.isSchema() && !config.getSchema().isBlank()) {
            hikari.setSchema(config.getSchema());
        }
        hikari.setConnectionTimeout(Math.max(
                HIKARI_MIN_CONNECTION_TIMEOUT_MILLIS,
                defaultBorrowTimeout.toMillis()
        ));
        hikari.setMaxLifetime(Math.max(
                HIKARI_MIN_MAX_LIFETIME_MILLIS,
                maxLifetime.toMillis()
        ));
        hikari.setInitializationFailTimeout(0);
        return hikari;
    }

    private void release(PooledAgtySQL lease) {
        if (activeLeases.remove(lease)) {
            permits.release();
        }
    }

    private static void closeAfterFailedBorrow(Connection connection, Exception failure) {
        try {
            connection.close();
        } catch (SQLException closeException) {
            failure.addSuppressed(closeException);
        }
    }

    private Connection guard(Connection connection) {
        InvocationHandler handler = new InvocationHandler() {
            private final AtomicBoolean closedHandle = new AtomicBoolean(false);

            @Override
            public Object invoke(Object proxyObject, Method method, Object[] args) throws Throwable {
                String methodName = method.getName();
                if ("close".equals(methodName)) {
                    if (closedHandle.compareAndSet(false, true)) {
                        connection.close();
                    }
                    return null;
                }
                if ("isClosed".equals(methodName)) {
                    return closedHandle.get() || closed.get() || connection.isClosed();
                }
                if ("unwrap".equals(methodName)) {
                    Class<?> target = unwrapTarget(args);
                    if (target.isInstance(proxyObject)) {
                        return proxyObject;
                    }
                    throw new SQLException("Unwrapping a pooled physical connection is disabled");
                }
                if ("isWrapperFor".equals(methodName)) {
                    return unwrapTarget(args).isInstance(proxyObject);
                }
                if ("toString".equals(methodName)) {
                    return "AgtySQLPoolConnection[guarded]";
                }
                if ("hashCode".equals(methodName)) {
                    return System.identityHashCode(proxyObject);
                }
                if ("equals".equals(methodName)) {
                    return proxyObject == (args == null ? null : args[0]);
                }
                if (closedHandle.get() || closed.get()) {
                    throw new SQLException("Connection handle is closed");
                }
                try {
                    return method.invoke(connection, args);
                } catch (InvocationTargetException exception) {
                    throw exception.getCause();
                }
            }
        };
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                handler
        );
    }

    private static Class<?> unwrapTarget(Object[] args) throws SQLException {
        if (args == null || args.length != 1 || !(args[0] instanceof Class<?> target)) {
            throw new SQLException("Invalid unwrap target");
        }
        return target;
    }

    private void ensureOpen() {
        if (closed.get()) {
            throw new IllegalStateException("Pool closed");
        }
    }

    private static void requirePositive(Duration value, String name) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }

    public static final class PooledAgtySQL implements AutoCloseable {
        private final AgtySQL delegate;
        private final AgtySQLPool pool;
        private final AtomicBoolean closed = new AtomicBoolean(false);

        private PooledAgtySQL(AgtySQL delegate, AgtySQLPool pool) {
            this.delegate = delegate;
            this.pool = pool;
        }

        public AgtySQL sql() {
            if (closed.get() || pool.closed.get()) {
                throw new IllegalStateException("Pooled AgtySQL handle is closed");
            }
            return delegate;
        }

        private boolean isHealthy() {
            try {
                Connection connection = delegate.getConnection();
                return !connection.isClosed() && connection.isValid(2);
            } catch (SQLException exception) {
                return false;
            }
        }

        @Override
        public void close() {
            closeLease();
        }

        private void closeFromPool() {
            closeLease();
        }

        private void closeLease() {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            try {
                delegate.close();
            } finally {
                pool.release(this);
            }
        }
    }
}
