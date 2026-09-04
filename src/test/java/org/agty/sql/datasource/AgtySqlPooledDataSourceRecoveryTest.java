package org.agty.sql.datasource;

import org.agty.sql.config.AgtySqlConfig;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AgtySqlPooledDataSourceRecoveryTest {

    @Test
    void poolRecoversAfterInitialConnectionCreationFailure() throws Exception {
        FlakyDataSource source = new FlakyDataSource();
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

            source.available.set(true);
            Connection recovered = awaitRecovery(pool);
            assertNotNull(recovered);
            try (Connection connection = recovered) {
                assertFalse(connection.isClosed());
            }
        }
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

    private static final class FlakyDataSource implements DataSource {
        private final AtomicBoolean available = new AtomicBoolean(false);

        @Override
        public Connection getConnection() throws SQLException {
            if (!available.get()) {
                throw new SQLException("Simulated connection creation failure");
            }
            return DriverManager.getConnection("jdbc:h2:mem:pool_recovery;MODE=MySQL");
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
    }
}
