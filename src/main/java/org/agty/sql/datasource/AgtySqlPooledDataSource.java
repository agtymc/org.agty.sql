package org.agty.sql.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.agty.sql.config.AgtySqlConfig;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Spring-style pooled DataSource compatibility adapter backed by HikariCP.
 *
 * <p>The data source is thread-safe. Each returned connection handle belongs to
 * one borrower and must be closed after use.</p>
 */
public final class AgtySqlPooledDataSource implements DataSource, AutoCloseable {
    private static final AtomicLong POOL_SEQUENCE = new AtomicLong();
    private static final long HIKARI_MIN_CONNECTION_TIMEOUT_MILLIS = 250L;
    private static final long HIKARI_MIN_IDLE_TIMEOUT_MILLIS = 10_000L;
    private static final long HIKARI_MIN_MAX_LIFETIME_MILLIS = 30_000L;

    private final AgtySqlConfig config;
    private final DataSource source;
    private final HikariDataSource dataSource;
    private final int maxPoolSize;
    private final int minIdle;
    private final Set<GuardedConnectionHandler> activeHandles = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private volatile PrintWriter logWriter;
    private volatile int loginTimeout;

    public AgtySqlPooledDataSource(
            AgtySqlConfig config,
            int maxPoolSize,
            int minIdle,
            long connectionTimeoutMillis,
            long idleTimeoutMillis,
            long maxLifetimeMillis
    ) {
        this(
                config,
                maxPoolSize,
                minIdle,
                Duration.ofMillis(connectionTimeoutMillis),
                Duration.ofMillis(idleTimeoutMillis),
                Duration.ofMillis(maxLifetimeMillis)
        );
    }

    public AgtySqlPooledDataSource(
            AgtySqlConfig config,
            int maxPoolSize,
            int minIdle,
            Duration connectionTimeout,
            Duration idleTimeout,
            Duration maxLifetime
    ) {
        this(
                config,
                config == null ? null : new AgtySqlDataSource(config),
                maxPoolSize,
                minIdle,
                connectionTimeout,
                idleTimeout,
                maxLifetime
        );
    }

    AgtySqlPooledDataSource(
            AgtySqlConfig config,
            DataSource source,
            int maxPoolSize,
            int minIdle,
            Duration connectionTimeout,
            Duration idleTimeout,
            Duration maxLifetime
    ) {
        if (config == null) {
            throw new IllegalArgumentException("AgtySqlConfig must not be null");
        }
        if (source == null) {
            throw new IllegalArgumentException("DataSource must not be null");
        }
        if (maxPoolSize < 1) {
            throw new IllegalArgumentException("maxPoolSize must be greater than zero");
        }
        if (minIdle < 0 || minIdle > maxPoolSize) {
            throw new IllegalArgumentException("minIdle must be between zero and maxPoolSize");
        }
        requirePositive(connectionTimeout, "connectionTimeout");
        requirePositive(idleTimeout, "idleTimeout");
        requirePositive(maxLifetime, "maxLifetime");

        this.config = AgtySqlConfig.getClone(config);
        this.source = source;
        this.maxPoolSize = maxPoolSize;
        this.minIdle = minIdle;
        this.dataSource = new HikariDataSource(createHikariConfig(
                connectionTimeout,
                idleTimeout,
                maxLifetime
        ));
    }

    public AgtySqlConfig getConfig() {
        return AgtySqlConfig.getClone(config);
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public int getTotalConnections() {
        HikariPoolMXBean pool = dataSource.getHikariPoolMXBean();
        return pool == null ? 0 : pool.getTotalConnections();
    }

    public int getIdleConnections() {
        HikariPoolMXBean pool = dataSource.getHikariPoolMXBean();
        return pool == null ? 0 : pool.getIdleConnections();
    }

    public int getActiveConnections() {
        return activeHandles.size();
    }

    @Override
    public Connection getConnection() throws SQLException {
        ensureOpen();
        Connection delegate = dataSource.getConnection();
        if (closed.get()) {
            delegate.close();
            throw new SQLException("DataSource is closed");
        }

        GuardedConnectionHandler handler = new GuardedConnectionHandler(delegate);
        activeHandles.add(handler);
        if (closed.get()) {
            handler.closeHandle();
            throw new SQLException("DataSource is closed");
        }
        return handler.proxy();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new SQLFeatureNotSupportedException(
                "Per-call credentials are not supported by AgtySqlPooledDataSource"
        );
    }

    @Override
    public PrintWriter getLogWriter() {
        return logWriter;
    }

    @Override
    public void setLogWriter(PrintWriter out) {
        try {
            dataSource.setLogWriter(out);
            this.logWriter = out;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to configure the JDBC log writer", e);
        }
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        if (seconds < 0) {
            throw new SQLException("Login timeout must not be negative");
        }
        this.loginTimeout = seconds;
        source.setLoginTimeout(seconds);
        dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() {
        return loginTimeout;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return source.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface != null && iface.isInstance(this)) {
            return iface.cast(this);
        }
        throw new SQLException("Unsupported unwrap: " + (iface == null ? "null" : iface.getName()));
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface != null && iface.isInstance(this);
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        for (GuardedConnectionHandler handle : Set.copyOf(activeHandles)) {
            handle.closeHandle();
        }
        dataSource.close();
    }

    private HikariConfig createHikariConfig(
            Duration connectionTimeout,
            Duration idleTimeout,
            Duration maxLifetime
    ) {
        HikariConfig hikari = new HikariConfig();
        hikari.setPoolName("agty-sql-datasource-" + POOL_SEQUENCE.incrementAndGet());
        hikari.setDataSource(source);
        hikari.setMaximumPoolSize(maxPoolSize);
        hikari.setMinimumIdle(minIdle);
        hikari.setAutoCommit(config.isAutoCommit());
        if (config.isSchema() && !config.getSchema().isBlank()) {
            hikari.setSchema(config.getSchema());
        }
        hikari.setConnectionTimeout(Math.max(
                HIKARI_MIN_CONNECTION_TIMEOUT_MILLIS,
                connectionTimeout.toMillis()
        ));
        hikari.setIdleTimeout(Math.max(HIKARI_MIN_IDLE_TIMEOUT_MILLIS, idleTimeout.toMillis()));
        hikari.setMaxLifetime(Math.max(HIKARI_MIN_MAX_LIFETIME_MILLIS, maxLifetime.toMillis()));
        hikari.setInitializationFailTimeout(0);
        return hikari;
    }

    private void ensureOpen() throws SQLException {
        if (closed.get()) {
            throw new SQLException("DataSource is closed");
        }
    }

    private static void requirePositive(Duration value, String name) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }

    private final class GuardedConnectionHandler implements InvocationHandler {
        private final Connection delegate;
        private final AtomicBoolean closedHandle = new AtomicBoolean(false);
        private final Connection proxy;

        private GuardedConnectionHandler(Connection delegate) {
            this.delegate = delegate;
            this.proxy = (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    this
            );
        }

        private Connection proxy() {
            return proxy;
        }

        @Override
        public Object invoke(Object proxyObject, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if ("close".equals(methodName)) {
                closeHandle();
                return null;
            }
            if ("isClosed".equals(methodName)) {
                return closedHandle.get() || closed.get() || delegate.isClosed();
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
                return "AgtySqlPooledConnection[guarded]";
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
                return method.invoke(delegate, args);
            } catch (InvocationTargetException exception) {
                throw exception.getCause();
            }
        }

        private Class<?> unwrapTarget(Object[] args) throws SQLException {
            if (args == null || args.length != 1 || !(args[0] instanceof Class<?> target)) {
                throw new SQLException("Invalid unwrap target");
            }
            return target;
        }

        private void closeHandle() {
            if (!closedHandle.compareAndSet(false, true)) {
                return;
            }
            activeHandles.remove(this);
            try {
                delegate.close();
            } catch (SQLException exception) {
                PrintWriter writer = logWriter;
                if (writer != null) {
                    exception.printStackTrace(writer);
                }
            }
        }
    }
}
