package org.agty.sql;

import org.agty.sql.config.AgtySqlConfig;
import org.agty.sql.datasource.AgtySqlPooledDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class AgtySqlPooledDataSourceIntegrationTest {

    @Test
    void pooledDataSourceWorksAsSpringStyleJdbcDataSource() throws Exception {
        Path databasePath = newDatabasePath("spring-style");
        AgtySqlPooledDataSource dataSource = createDataSource(databasePath, 2, 1);

        try {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS session_data");
                statement.execute("CREATE TABLE session_data (id BIGINT PRIMARY KEY, session_value VARCHAR(255))");
            }

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement insert = connection.prepareStatement(
                         "INSERT INTO session_data (id, session_value) VALUES (?, ?)"
                 )) {
                insert.setLong(1, 1L);
                insert.setString(2, "first");
                Assertions.assertEquals(1, insert.executeUpdate());
            }

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement select = connection.prepareStatement(
                         "SELECT session_value FROM session_data WHERE id = ?"
                 )) {
                select.setLong(1, 1L);
                try (ResultSet resultSet = select.executeQuery()) {
                    Assertions.assertTrue(resultSet.next());
                    Assertions.assertEquals("first", resultSet.getString("session_value"));
                }
            }
        } finally {
            dataSource.close();
            deleteDatabaseFiles(databasePath);
        }
    }

    @Test
    void pooledDataSourceRespectsPoolLimitAndReusesReturnedConnection() throws Exception {
        Path databasePath = newDatabasePath("reuse");
        AgtySqlPooledDataSource dataSource = createDataSource(databasePath, 2, 1);

        try (Connection first = dataSource.getConnection();
             Connection second = dataSource.getConnection()) {
            Assertions.assertEquals(2, dataSource.getActiveConnections());
            Assertions.assertEquals(2, dataSource.getTotalConnections());
            Assertions.assertThrows(SQLException.class, dataSource::getConnection);
        } finally {
            dataSource.close();
            deleteDatabaseFiles(databasePath);
        }

        dataSource = createDataSource(databasePath, 2, 1);
        try {
            Connection first = dataSource.getConnection();
            first.close();

            Assertions.assertEquals(0, dataSource.getActiveConnections());
            Assertions.assertTrue(dataSource.getIdleConnections() >= 1);

            try (Connection reused = dataSource.getConnection()) {
                Assertions.assertEquals(1, dataSource.getActiveConnections());
                Assertions.assertTrue(dataSource.getTotalConnections() <= 2);
                Assertions.assertFalse(reused.isClosed());
            }
        } finally {
            dataSource.close();
            deleteDatabaseFiles(databasePath);
        }
    }

    @Test
    void pooledDataSourceCanBeUsedThroughDataSourceInterface() throws Exception {
        Path databasePath = newDatabasePath("interface");
        DataSource dataSource = createDataSource(databasePath, 3, 2);

        try (Connection connection = dataSource.getConnection()) {
            Assertions.assertFalse(connection.isClosed());
        } finally {
            ((AgtySqlPooledDataSource) dataSource).close();
            deleteDatabaseFiles(databasePath);
        }
    }

    @Test
    void connectionHandleCloseIsConcurrentSafeAndCannotExposePhysicalConnection() throws Exception {
        Path databasePath = newDatabasePath("guard");
        AgtySqlPooledDataSource dataSource = createDataSource(databasePath, 1, 0);
        Connection connection = dataSource.getConnection();

        Assertions.assertThrows(
                SQLException.class,
                () -> connection.unwrap(org.h2.jdbc.JdbcConnection.class)
        );

        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> closes = new ArrayList<>();
        try {
            for (int index = 0; index < 4; index++) {
                closes.add(executor.submit(() -> {
                    start.await();
                    connection.close();
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> close : closes) {
                close.get();
            }
            Assertions.assertTrue(connection.isClosed());
            Assertions.assertEquals(0, dataSource.getActiveConnections());

            try (Connection current = dataSource.getConnection()) {
                connection.close();
                Assertions.assertThrows(SQLException.class, connection::createStatement);
                Assertions.assertFalse(current.isClosed());
                Assertions.assertThrows(SQLException.class, dataSource::getConnection);
            }
        } finally {
            executor.shutdownNow();
            dataSource.close();
            deleteDatabaseFiles(databasePath);
        }
    }

    @Test
    void returnedConnectionRollsBackAndPoolCloseInvalidatesActiveHandle() throws Exception {
        Path databasePath = newDatabasePath("state-reset");
        AgtySqlPooledDataSource dataSource = createDataSource(databasePath, 1, 0);
        try {
            try (Connection setup = dataSource.getConnection();
                 Statement statement = setup.createStatement()) {
                statement.execute("CREATE TABLE tx_data (id BIGINT PRIMARY KEY)");
            }

            Connection transaction = dataSource.getConnection();
            String originalSchema = transaction.getSchema();
            int originalIsolation = transaction.getTransactionIsolation();
            transaction.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            transaction.setSchema("INFORMATION_SCHEMA");
            transaction.setAutoCommit(false);
            try (Statement statement = transaction.createStatement()) {
                statement.executeUpdate("INSERT INTO PUBLIC.tx_data (id) VALUES (1)");
            }
            transaction.close();

            Connection verification = dataSource.getConnection();
            Assertions.assertTrue(verification.getAutoCommit());
            Assertions.assertEquals(originalSchema, verification.getSchema());
            Assertions.assertEquals(originalIsolation, verification.getTransactionIsolation());
            try (Statement statement = verification.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM tx_data")) {
                Assertions.assertTrue(resultSet.next());
                Assertions.assertEquals(0, resultSet.getInt(1));
            }

            dataSource.close();
            Assertions.assertTrue(verification.isClosed());
            Assertions.assertThrows(SQLException.class, verification::createStatement);
            Assertions.assertThrows(SQLException.class, dataSource::getConnection);
        } finally {
            dataSource.close();
            deleteDatabaseFiles(databasePath);
        }
    }

    private AgtySqlPooledDataSource createDataSource(Path databasePath, int maxPoolSize, int minIdle) {
        AgtySqlConfig config = new AgtySqlConfig()
                .setDriver("h2")
                .setDatabase(databasePath.toString())
                .setSchema("PUBLIC")
                .setPfx("")
                .setThrowException(true)
                .setDebug(false);

        return new AgtySqlPooledDataSource(
                config,
                maxPoolSize,
                minIdle,
                Duration.ofMillis(50),
                Duration.ofMinutes(5),
                Duration.ofMinutes(5)
        );
    }

    private Path newDatabasePath(String prefix) {
        return Path.of("target", "test-datasource", prefix + "-" + UUID.randomUUID().toString().replace("-", ""));
    }

    private void deleteDatabaseFiles(Path databasePath) throws Exception {
        Files.deleteIfExists(Path.of(databasePath + ".mv.db"));
        Files.deleteIfExists(Path.of(databasePath + ".trace.db"));
    }
}
