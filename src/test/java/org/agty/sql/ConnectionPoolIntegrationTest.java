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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
