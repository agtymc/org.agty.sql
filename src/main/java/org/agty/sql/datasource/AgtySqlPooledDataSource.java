package org.agty.sql.datasource;

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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Connection-level pooled DataSource intended to be used as a Spring-style
 * application DataSource bean.
 */
public final class AgtySqlPooledDataSource implements DataSource, AutoCloseable {

    private final AgtySqlDataSource source;
    private final int maxPoolSize;
    private final int minIdle;
    private final Duration connectionTimeout;
    private final Duration idleTimeout;
    private final Duration maxLifetime;

    private final Object monitor = new Object();
    private final Deque<PooledConnection> idleConnections = new ArrayDeque<>();
    private final Set<PooledConnection> activeConnections = new HashSet<>();
    private final ScheduledExecutorService housekeeper;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private volatile int totalConnections;
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
                new AgtySqlDataSource(config),
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
                new AgtySqlDataSource(config),
                maxPoolSize,
                minIdle,
                connectionTimeout,
                idleTimeout,
                maxLifetime
        );
    }

    private AgtySqlPooledDataSource(
            AgtySqlDataSource source,
            int maxPoolSize,
            int minIdle,
            Duration connectionTimeout,
            Duration idleTimeout,
            Duration maxLifetime
    ) {
        if (maxPoolSize < 1) {
            throw new IllegalArgumentException("maxPoolSize must be greater than zero");
        }
        if (minIdle < 0 || minIdle > maxPoolSize) {
            throw new IllegalArgumentException("minIdle must be between zero and maxPoolSize");
        }
        if (connectionTimeout == null || connectionTimeout.isZero() || connectionTimeout.isNegative()) {
            throw new IllegalArgumentException("connectionTimeout must be positive");
        }
        if (idleTimeout == null || idleTimeout.isZero() || idleTimeout.isNegative()) {
            throw new IllegalArgumentException("idleTimeout must be positive");
        }
        if (maxLifetime == null || maxLifetime.isZero() || maxLifetime.isNegative()) {
            throw new IllegalArgumentException("maxLifetime must be positive");
        }

        this.source = source;
        this.maxPoolSize = maxPoolSize;
        this.minIdle = minIdle;
        this.connectionTimeout = connectionTimeout;
        this.idleTimeout = idleTimeout;
        this.maxLifetime = maxLifetime;

        this.housekeeper = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "AgtySqlPooledDataSource-Housekeeper");
            thread.setDaemon(true);
            return thread;
        });

        prefillMinIdle();

        long housekeeperPeriodMillis = Math.max(250L, Math.min(idleTimeout.toMillis(), 1000L));
        housekeeper.scheduleAtFixedRate(this::housekeep, housekeeperPeriodMillis, housekeeperPeriodMillis, TimeUnit.MILLISECONDS);
    }

    public AgtySqlConfig getConfig() {
        return source.getConfig();
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public int getTotalConnections() {
        return totalConnections;
    }

    public int getIdleConnections() {
        synchronized (monitor) {
            return idleConnections.size();
        }
    }

    public int getActiveConnections() {
        synchronized (monitor) {
            return activeConnections.size();
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return borrowConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new SQLFeatureNotSupportedException("Per-call credentials are not supported by AgtySqlPooledDataSource");
    }

    @Override
    public PrintWriter getLogWriter() {
        return logWriter;
    }

    @Override
    public void setLogWriter(PrintWriter out) {
        this.logWriter = out;
        source.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        this.loginTimeout = seconds;
        source.setLoginTimeout(seconds);
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
        if (iface.isInstance(this)) {
            return iface.cast(this);
        }
        throw new SQLException("Unsupported unwrap: " + iface.getName());
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isInstance(this);
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        housekeeper.shutdownNow();

        synchronized (monitor) {
            for (PooledConnection idle : idleConnections) {
                closeQuietly(idle.physicalConnection);
            }
            idleConnections.clear();

            for (PooledConnection active : activeConnections) {
                active.markPoolClosed();
            }
        }
    }

    private Connection borrowConnection() throws SQLException {
        long deadline = System.nanoTime() + connectionTimeout.toNanos();

        while (true) {
            PooledConnection pooled = tryBorrow();
            if (pooled != null) {
                return pooled.borrowHandle();
            }

            long remainingNanos = deadline - System.nanoTime();
            if (remainingNanos <= 0) {
                throw new SQLException("Connection timeout");
            }

            synchronized (monitor) {
                if (closed.get()) {
                    throw new SQLException("DataSource is closed");
                }
                try {
                    TimeUnit.NANOSECONDS.timedWait(monitor, remainingNanos);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Interrupted while waiting for a pooled connection", e);
                }
            }
        }
    }

    private PooledConnection tryBorrow() throws SQLException {
        PooledConnection pooled;

        synchronized (monitor) {
            if (closed.get()) {
                throw new SQLException("DataSource is closed");
            }

            while ((pooled = idleConnections.pollFirst()) != null) {
                if (shouldDiscard(pooled)) {
                    destroyConnection(pooled);
                    continue;
                }

                activeConnections.add(pooled);
                pooled.inUse = true;
                return pooled;
            }

            if (totalConnections >= maxPoolSize) {
                return null;
            }

            totalConnections++;
        }

        try {
            PooledConnection created = new PooledConnection(source.getConnection());
            synchronized (monitor) {
                if (closed.get()) {
                    closeQuietly(created.physicalConnection);
                    totalConnections--;
                    throw new SQLException("DataSource is closed");
                }
                activeConnections.add(created);
                created.inUse = true;
            }
            return created;
        } catch (SQLException e) {
            synchronized (monitor) {
                totalConnections--;
                monitor.notifyAll();
            }
            throw e;
        }
    }

    private void returnConnection(PooledConnection pooled) {
        synchronized (monitor) {
            if (!activeConnections.remove(pooled)) {
                return;
            }

            pooled.inUse = false;
            pooled.lastReleasedAt = System.currentTimeMillis();

            if (closed.get() || shouldDiscard(pooled) || !resetConnectionState(pooled.physicalConnection)) {
                destroyConnection(pooled);
            } else {
                idleConnections.addLast(pooled);
            }

            monitor.notifyAll();
        }
    }

    private void housekeep() {
        synchronized (monitor) {
            if (closed.get()) {
                return;
            }

            while (!idleConnections.isEmpty()) {
                PooledConnection pooled = idleConnections.peekFirst();
                if (pooled == null) {
                    break;
                }

                if (idleConnections.size() <= minIdle && !isExpiredByLifetime(pooled)) {
                    break;
                }

                if (shouldDiscard(pooled)) {
                    idleConnections.pollFirst();
                    destroyConnection(pooled);
                    continue;
                }

                break;
            }
        }

        ensureMinIdle();
    }

    private void prefillMinIdle() {
        ensureMinIdle();
    }

    private void ensureMinIdle() {
        while (true) {
            synchronized (monitor) {
                if (closed.get()) {
                    return;
                }
                if (idleConnections.size() >= minIdle || totalConnections >= maxPoolSize) {
                    return;
                }
                totalConnections++;
            }

            try {
                PooledConnection created = new PooledConnection(source.getConnection());
                synchronized (monitor) {
                    if (closed.get()) {
                        closeQuietly(created.physicalConnection);
                        totalConnections--;
                        return;
                    }
                    idleConnections.addLast(created);
                    monitor.notifyAll();
                }
            } catch (SQLException e) {
                synchronized (monitor) {
                    totalConnections--;
                    monitor.notifyAll();
                }
                return;
            }
        }
    }

    private boolean shouldDiscard(PooledConnection pooled) {
        return pooled.physicalConnection == null
                || isExpiredByLifetime(pooled)
                || isExpiredByIdle(pooled)
                || !isConnectionHealthy(pooled.physicalConnection);
    }

    private boolean isExpiredByLifetime(PooledConnection pooled) {
        return System.currentTimeMillis() - pooled.createdAt > maxLifetime.toMillis();
    }

    private boolean isExpiredByIdle(PooledConnection pooled) {
        return System.currentTimeMillis() - pooled.lastReleasedAt > idleTimeout.toMillis();
    }

    private boolean isConnectionHealthy(Connection connection) {
        try {
            return !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean resetConnectionState(Connection connection) {
        try {
            if (!connection.getAutoCommit()) {
                connection.rollback();
                connection.setAutoCommit(true);
            }
            if (connection.isReadOnly()) {
                connection.setReadOnly(false);
            }
            connection.clearWarnings();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private void destroyConnection(PooledConnection pooled) {
        closeQuietly(pooled.physicalConnection);
        totalConnections--;
    }

    private void closeQuietly(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }

    private final class PooledConnection {
        private final Connection physicalConnection;
        private final long createdAt = System.currentTimeMillis();
        private volatile long lastReleasedAt = createdAt;
        private volatile boolean inUse;
        private volatile boolean poolClosed;

        private PooledConnection(Connection physicalConnection) {
            this.physicalConnection = physicalConnection;
        }

        private Connection borrowHandle() {
            InvocationHandler handler = new PooledConnectionInvocationHandler(this);
            return (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    handler
            );
        }

        private void markPoolClosed() {
            poolClosed = true;
        }
    }

    private final class PooledConnectionInvocationHandler implements InvocationHandler {
        private final PooledConnection pooled;
        private volatile boolean closedHandle;

        private PooledConnectionInvocationHandler(PooledConnection pooled) {
            this.pooled = pooled;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();

            if ("close".equals(methodName)) {
                if (!closedHandle) {
                    closedHandle = true;
                    returnConnection(pooled);
                }
                return null;
            }

            if ("isClosed".equals(methodName)) {
                return closedHandle || pooled.poolClosed || pooled.physicalConnection.isClosed();
            }

            if ("unwrap".equals(methodName) && args != null && args.length == 1 && args[0] instanceof Class<?> target) {
                if (target.isInstance(proxy)) {
                    return proxy;
                }
                return pooled.physicalConnection.unwrap(target);
            }

            if ("isWrapperFor".equals(methodName) && args != null && args.length == 1 && args[0] instanceof Class<?> target) {
                return target.isInstance(proxy) || pooled.physicalConnection.isWrapperFor(target);
            }

            if ("toString".equals(methodName)) {
                return "AgtySqlPooledConnection[" + pooled.physicalConnection + "]";
            }

            if ("hashCode".equals(methodName)) {
                return System.identityHashCode(proxy);
            }

            if ("equals".equals(methodName)) {
                return proxy == (args == null ? null : args[0]);
            }

            if (closedHandle || pooled.poolClosed) {
                throw new SQLException("Connection handle is closed");
            }

            try {
                return method.invoke(pooled.physicalConnection, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }
}
