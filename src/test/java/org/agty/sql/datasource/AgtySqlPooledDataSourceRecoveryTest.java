package org.agty.sql.datasource;

import org.agty.sql.config.AgtySqlConfig;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLRecoverableException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgtySqlPooledDataSourceRecoveryTest {

    @Test
    void poolRecoversAfterInitialConnectionCreationFailure() throws Exception {
        RestartableDataSource source = new RestartableDataSource();
        AgtySqlConfig config = new AgtySqlConfig()
                .setDriver("h2")
                .setDatabase("unused")
                .setPfx("");

        try (AgtySqlPooledDataSource pool = new AgtySqlPooledDataSource(
                config,
                source,
                1,
                0,
                Duration.ofMillis(250),
                Duration.ofMinutes(1),
                Duration.ofMinutes(1)
        )) {
            assertThrows(SQLException.class, pool::getConnection);

            source.start();
            Connection recovered = awaitRecovery(pool);
            assertNotNull(recovered);
            try (Connection connection = recovered) {
                assertFalse(connection.isClosed());
            }
        }
    }

    @Test
    void networkInterruptionEvictsLeaseAndAllowsRecovery() throws Exception {
        RestartableDataSource source = new RestartableDataSource();
        source.start();

        try (AgtySqlPooledDataSource pool = createPool(source)) {
            Connection interrupted = pool.getConnection();
            source.disconnectNetwork();

            assertThrows(SQLRecoverableException.class, interrupted::createStatement);
            interrupted.close();
            assertThrows(SQLException.class, pool::getConnection);

            source.start();
            try (Connection recovered = awaitRecovery(pool);
                 Statement statement = recovered.createStatement()) {
                assertFalse(recovered.isClosed());
                assertFalse(statement.execute(
                        "CREATE TABLE IF NOT EXISTS network_recovery (id INT PRIMARY KEY)"
                ));
            }
        }
    }

    @Test
    void poolRecoversAcrossDatabaseRestartCycles() throws Exception {
        RestartableDataSource source = new RestartableDataSource();
        source.start();

        try (AgtySqlPooledDataSource pool = createPool(source)) {
            for (int cycle = 0; cycle < 3; cycle++) {
                Connection stale = pool.getConnection();
                source.stopDatabase();

                assertThrows(SQLException.class, stale::createStatement);
                stale.close();
                assertThrows(SQLException.class, pool::getConnection);

                source.start();
                try (Connection recovered = awaitRecovery(pool);
                     Statement statement = recovered.createStatement()) {
                    assertFalse(recovered.isClosed());
                    try (var resultSet = statement.executeQuery("SELECT 1")) {
                        assertTrue(resultSet.next());
                        assertEquals(1, resultSet.getInt(1));
                    }
                }
            }
            assertEquals(0, pool.getActiveConnections());
        }
    }

    private AgtySqlPooledDataSource createPool(DataSource source) {
        AgtySqlConfig config = new AgtySqlConfig()
                .setDriver("h2")
                .setDatabase("unused")
                .setPfx("");
        return new AgtySqlPooledDataSource(
                config,
                source,
                1,
                0,
                Duration.ofMillis(250),
                Duration.ofMinutes(1),
                Duration.ofMinutes(1)
        );
    }

    private Connection awaitRecovery(AgtySqlPooledDataSource pool) throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(4).toNanos();
        SQLException lastFailure = null;
        while (System.nanoTime() < deadline) {
            try {
                return pool.getConnection();
            } catch (SQLException exception) {
                lastFailure = exception;
                Thread.sleep(100);
            }
        }
        throw lastFailure == null ? new SQLException("Pool did not recover") : lastFailure;
    }

    private static final class RestartableDataSource implements DataSource {
        private final AtomicBoolean available = new AtomicBoolean(false);
        private final Set<Connection> physicalConnections = ConcurrentHashMap.newKeySet();
        private final String databaseName = "pool_recovery_" + UUID.randomUUID().toString().replace("-", "");

        @Override
        public Connection getConnection() throws SQLException {
            if (!available.get()) {
                throw new SQLException("Simulated connection creation failure");
            }
            Connection physical = DriverManager.getConnection(
                    "jdbc:h2:mem:" + databaseName + ";MODE=MySQL;DB_CLOSE_DELAY=-1"
            );
            physicalConnections.add(physical);
            return networkGuard(physical);
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return getConnection();
        }

        @Override
        public PrintWriter getLogWriter() {
            return null;
        }

        @Override
        public void setLogWriter(PrintWriter out) {
        }

        @Override
        public void setLoginTimeout(int seconds) {
        }

        @Override
        public int getLoginTimeout() {
            return 0;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            throw new SQLException("Unsupported unwrap");
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) {
            return false;
        }

        private void start() {
            available.set(true);
        }

        private void disconnectNetwork() {
            available.set(false);
        }

        private void stopDatabase() {
            available.set(false);
            for (Connection connection : Set.copyOf(physicalConnections)) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                    // The injected restart is allowed to race with pool cleanup.
                } finally {
                    physicalConnections.remove(connection);
                }
            }
        }

        private Connection networkGuard(Connection physical) {
            return (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    (proxy, method, args) -> invokeGuarded(physical, proxy, method, args)
            );
        }

        private Object invokeGuarded(
                Connection physical,
                Object proxy,
                Method method,
                Object[] args
        ) throws Throwable {
            String methodName = method.getName();
            if ("close".equals(methodName)) {
                physicalConnections.remove(physical);
                physical.close();
                return null;
            }
            if ("isClosed".equals(methodName)) {
                return physical.isClosed();
            }
            if ("isValid".equals(methodName) && !available.get()) {
                return false;
            }
            if ("toString".equals(methodName)) {
                return "RestartableConnection[" + databaseName + "]";
            }
            if ("hashCode".equals(methodName)) {
                return System.identityHashCode(proxy);
            }
            if ("equals".equals(methodName)) {
                return proxy == (args == null ? null : args[0]);
            }
            if (!available.get()) {
                throw new SQLRecoverableException("Simulated network interruption", "08006");
            }
            try {
                return method.invoke(physical, args);
            } catch (InvocationTargetException exception) {
                throw exception.getCause();
            }
        }
    }
}
