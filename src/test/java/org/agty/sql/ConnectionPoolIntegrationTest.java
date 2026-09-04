package org.agty.sql;

import org.agty.sql.config.AgtySqlConfig;
import org.agty.sql.data.Arguments;
import org.agty.sql.pool.AgtySQLPool;
import org.agty.sql.pool.ConnectionPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Connection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class ConnectionPoolIntegrationTest {

    private final List<String> poolNames = new ArrayList<>();
    private final List<Path> databasePaths = new ArrayList<>();

    @AfterEach
    void cleanup() throws Exception {
        ConnectionPool.closeAll();

        for (Path databasePath : databasePaths) {
            Files.deleteIfExists(Path.of(databasePath + ".mv.db"));
            Files.deleteIfExists(Path.of(databasePath + ".trace.db"));
        }
    }

    @Test
    void multiplePoolsBorrowIndependentlyAndRespectOwnLimits() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");

        String pool1Name = "pool-a-" + suffix;
        String pool2Name = "pool-b-" + suffix;
        String pool3Name = "pool-c-" + suffix;

        AgtySQLPool pool1 = registerPool(pool1Name, 2);
        AgtySQLPool pool2 = registerPool(pool2Name, 2);
        AgtySQLPool pool3 = registerPool(pool3Name, 3);

        try (AgtySQLPool.PooledAgtySQL pool1Conn1 = pool1.borrow();
             AgtySQLPool.PooledAgtySQL pool1Conn2 = pool1.borrow();
             AgtySQLPool.PooledAgtySQL pool2Conn1 = pool2.borrow();
             AgtySQLPool.PooledAgtySQL pool2Conn2 = pool2.borrow();
             AgtySQLPool.PooledAgtySQL pool3Conn1 = pool3.borrow();
             AgtySQLPool.PooledAgtySQL pool3Conn2 = pool3.borrow();
             AgtySQLPool.PooledAgtySQL pool3Conn3 = pool3.borrow()) {

            Assertions.assertThrows(SQLException.class, () -> pool1.borrow(Duration.ofMillis(25)));
            Assertions.assertThrows(SQLException.class, () -> pool2.borrow(Duration.ofMillis(25)));
            Assertions.assertThrows(SQLException.class, () -> pool3.borrow(Duration.ofMillis(25)));

            initializePoolTable(pool1Conn1.sql(), "{pool_items}");
            initializePoolTable(pool2Conn1.sql(), "{pool_items}");
            initializePoolTable(pool3Conn1.sql(), "{pool_items}");

            insertRow(pool1Conn1.sql(), "{pool_items}", 1L, "pool-1");
            insertRow(pool2Conn1.sql(), "{pool_items}", 2L, "pool-2");
            insertRow(pool3Conn1.sql(), "{pool_items}", 3L, "pool-3");

            Assertions.assertEquals(1L, pool1Conn2.sql().countRows(Arguments.builder().setTable("{pool_items}")));
            Assertions.assertEquals(1L, pool2Conn2.sql().countRows(Arguments.builder().setTable("{pool_items}")));
            Assertions.assertEquals(1L, pool3Conn2.sql().countRows(Arguments.builder().setTable("{pool_items}")));

            Assertions.assertEquals("pool-1", pool1Conn2.sql().fetch(Arguments.builder().setTable("{pool_items}").setWhere("[id] = %d", 1)).getString("string"));
            Assertions.assertEquals("pool-2", pool2Conn2.sql().fetch(Arguments.builder().setTable("{pool_items}").setWhere("[id] = %d", 2)).getString("string"));
            Assertions.assertEquals("pool-3", pool3Conn3.sql().fetch(Arguments.builder().setTable("{pool_items}").setWhere("[id] = %d", 3)).getString("string"));

            Assertions.assertFalse(pool1Conn2.sql().rowIsExists(Arguments.builder().setTable("{pool_items}").setWhere("[id] = %d", 2)));
            Assertions.assertFalse(pool1Conn2.sql().rowIsExists(Arguments.builder().setTable("{pool_items}").setWhere("[id] = %d", 3)));
            Assertions.assertFalse(pool2Conn2.sql().rowIsExists(Arguments.builder().setTable("{pool_items}").setWhere("[id] = %d", 1)));
            Assertions.assertFalse(pool3Conn2.sql().rowIsExists(Arguments.builder().setTable("{pool_items}").setWhere("[id] = %d", 1)));
        }

        try (AgtySQLPool.PooledAgtySQL borrowedAgain = pool1.borrow(Duration.ofMillis(100))) {
            Assertions.assertEquals(1L, borrowedAgain.sql().countRows(Arguments.builder().setTable("{pool_items}")));
            Assertions.assertEquals("pool-1", borrowedAgain.sql().fetch(Arguments.builder().setTable("{pool_items}").setWhere("[id] = %d", 1)).getString("string"));
        }
    }

    @Test
    void staleAndConcurrentCloseCannotReleaseAnotherLease() throws Exception {
        AgtySQLPool pool = registerPool("lease-" + randomSuffix(), 1);
        AgtySQLPool.PooledAgtySQL first = pool.borrow();
        Connection firstConnection = first.sql().getConnection();
        first.close();

        AgtySQLPool.PooledAgtySQL current = pool.borrow();
        first.close();
        Assertions.assertThrows(
                SQLException.class,
                () -> pool.borrow(Duration.ofMillis(25))
        );
        Assertions.assertThrows(IllegalStateException.class, first::sql);
        Assertions.assertTrue(firstConnection.isClosed());

        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<?>> closes = new ArrayList<>();
            for (int index = 0; index < 4; index++) {
                closes.add(executor.submit(() -> {
                    start.await();
                    current.close();
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> close : closes) {
                close.get();
            }
        } finally {
            executor.shutdownNow();
        }

        try (AgtySQLPool.PooledAgtySQL next = pool.borrow(Duration.ofMillis(100))) {
            Assertions.assertFalse(next.sql().getConnection().isClosed());
        }
    }

    @Test
    void returnedLeaseRollsBackAndRestoresAutoCommit() throws Exception {
        AgtySQLPool pool = registerPool("reset-" + randomSuffix(), 1);
        try (AgtySQLPool.PooledAgtySQL setup = pool.borrow()) {
            initializePoolTable(setup.sql(), "{pool_items}");
        }

        AgtySQLPool.PooledAgtySQL transaction = pool.borrow();
        transaction.sql().setAutoCommit(false);
        insertRow(transaction.sql(), "{pool_items}", 1L, "must-rollback");
        transaction.close();

        try (AgtySQLPool.PooledAgtySQL verification = pool.borrow()) {
            Assertions.assertTrue(verification.sql().isAutoCommit());
            Assertions.assertEquals(
                    0L,
                    verification.sql().countRows(Arguments.builder().setTable("{pool_items}"))
            );
        }
    }

    @Test
    void closingPoolInvalidatesActiveLeaseAndBlocksPhysicalUnwrap() throws Exception {
        AgtySQLPool pool = registerPool("close-" + randomSuffix(), 1);
        AgtySQLPool.PooledAgtySQL lease = pool.borrow();
        Connection connection = lease.sql().getConnection();

        Assertions.assertThrows(SQLException.class, () -> connection.unwrap(org.h2.jdbc.JdbcConnection.class));
        pool.close();

        Assertions.assertTrue(connection.isClosed());
        Assertions.assertThrows(IllegalStateException.class, lease::sql);
        Assertions.assertThrows(IllegalStateException.class, pool::borrow);
    }

    @Test
    void concurrentBorrowersNeverShareOneConnectionHandle() throws Exception {
        AgtySQLPool pool = registerPool("stress-" + randomSuffix(), 4);
        ExecutorService executor = Executors.newFixedThreadPool(12);
        Set<Connection> activeConnections = ConcurrentHashMap.newKeySet();
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> tasks = new ArrayList<>();

        try {
            for (int worker = 0; worker < 12; worker++) {
                tasks.add(executor.submit(() -> {
                    start.await();
                    for (int iteration = 0; iteration < 40; iteration++) {
                        try (AgtySQLPool.PooledAgtySQL lease = pool.borrow(Duration.ofSeconds(2))) {
                            Connection connection = lease.sql().getConnection();
                            Assertions.assertTrue(activeConnections.add(connection));
                            try (var statement = connection.createStatement();
                                 var resultSet = statement.executeQuery("SELECT 1")) {
                                Assertions.assertTrue(resultSet.next());
                            } finally {
                                activeConnections.remove(connection);
                            }
                        }
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> task : tasks) {
                task.get();
            }
        } finally {
            executor.shutdownNow();
        }
        Assertions.assertTrue(activeConnections.isEmpty());
    }

    private AgtySQLPool registerPool(String poolName, int maxSize) {
        Path databasePath = Path.of("target", "test-pools", poolName);
        databasePaths.add(databasePath);
        poolNames.add(poolName);

        ConnectionPool.register(
                poolName,
                () -> new AgtySqlConfig()
                        .setDriver("h2")
                        .setDatabase(databasePath.toString())
                        .setPfx("")
                        .setThrowException(true)
                        .setDebug(false),
                new ConnectionPool.PoolOptions(
                        maxSize,
                        Duration.ofMinutes(5),
                        Duration.ofMillis(50)
                )
        );

        return ConnectionPool.get(poolName);
    }

    private String randomSuffix() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void initializePoolTable(AgtySQL sql, String table) {
        sql.execute("DROP TABLE IF EXISTS " + table);
        sql.clearErrors();
        sql.execute("CREATE TABLE " + table + " (id BIGINT PRIMARY KEY, string VARCHAR(255))");
        Assertions.assertFalse(sql.hasErrors(), sql.getErrors());
        Assertions.assertTrue(sql.tableIsExists(Arguments.builder().setTable(table)));
    }

    private void insertRow(AgtySQL sql, String table, long id, String value) {
        long insertedId = sql.insert(
                Arguments.builder()
                        .setTable(table)
                        .setPrimaryKey("id")
                        .addData("id", id)
                        .addData("string", value)
                        .setReturnLastInsertId(true)
        );

        Assertions.assertEquals(id, insertedId);
        Assertions.assertFalse(sql.hasErrors(), sql.getErrors());
    }
}
