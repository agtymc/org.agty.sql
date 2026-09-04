package org.agty.sql;

import org.agty.sql.data.Arguments;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.support.TestDatabaseProfile;
import org.agty.sql.support.TestDatabaseProfiles;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedList;
import java.util.stream.Stream;

@Tag("integration")
class AgtySQLCrossDbIntegrationTest {

    private static Stream<TestDatabaseProfile> sqlServers() {
        return TestDatabaseProfiles.sqlProfiles();
    }

    @ParameterizedTest(name = "cross-db smoke: {0}")
    @MethodSource("sqlServers")
    void smokeTest(TestDatabaseProfile profile) {
        AgtySQL sql = profile.createSql();
        String table = profile.tableName();

        try {
            recreateTable(sql, profile);

            Assertions.assertTrue(sql.tableIsExists(Arguments.builder().setTable(table)));

            insertRows(sql, table);

            SqlRow fetched = sql.fetch(Arguments.builder().setTable(table).setWhere("[id] = %d", 2));
            Assertions.assertEquals(2L, fetched.getLong("id"));
            Assertions.assertEquals("value-2", fetched.getString("string"));
            Assertions.assertEquals(20, fetched.getInt("integers"));

            Assertions.assertTrue(sql.rowIsExists(Arguments.builder().setTable(table).setWhere("[id] = %d", 3)));

            Assertions.assertTrue(sql.update(
                    Arguments.builder()
                            .setTable(table)
                            .addData("string", "value-2-updated")
                            .addData("integers", 25)
                            .setWhere("[id] = %d", 2)
            ));

            SqlRow updated = sql.fetch(Arguments.builder().setTable(table).setWhere("[id] = %d", 2));
            Assertions.assertEquals("value-2-updated", updated.getString("string"));
            Assertions.assertEquals(25, updated.getInt("integers"));

            Assertions.assertTrue(sql.delete(Arguments.builder().setTable(table).setWhere("[id] = %d", 1)));
            Assertions.assertEquals(3L, sql.countRows(Arguments.builder().setTable(table)));

            SqlRow first = sql.getFirstRow(Arguments.builder().setTable(table).setActionField("id"));
            SqlRow last = sql.getLastRow(Arguments.builder().setTable(table).setActionField("id"));
            Assertions.assertEquals(2L, first.getLong("id"));
            Assertions.assertEquals(4L, last.getLong("id"));

            LinkedList<SqlRow> rows = sql.listArray(
                    Arguments.builder()
                            .setTable(table)
                            .setWhere("[id] >= %d", 2)
                            .setOrderBy("id ASC")
            );
            Assertions.assertEquals(3, rows.size());
            Assertions.assertEquals(2L, rows.getFirst().getLong("id"));
            Assertions.assertEquals(4L, rows.getLast().getLong("id"));
        } finally {
            dropTableQuietly(sql, table);
            sql.close();
        }
    }

    @ParameterizedTest(name = "column aliases: {0}")
    @MethodSource("sqlServers")
    void usesColumnLabelsForAliases(TestDatabaseProfile profile) {
        AgtySQL sql = profile.createSql();
        String table = profile.tableName();

        try {
            recreateTable(sql, profile);
            insertRows(sql, table);

            Arguments query = Arguments.builder().setQuery(
                    org.agty.sql.data.SqlExpression.trusted(
                            "SELECT [string] AS selected_value FROM " + table + " WHERE [id] = 2"
                    )
            );

            SqlRow fetched = sql.fetch(query);
            Assertions.assertEquals("value-2", fetched.getString("selected_value"));
            Assertions.assertNull(fetched.getObject("string"));

            LinkedList<SqlRow> listed = sql.listArray(query);
            Assertions.assertEquals("value-2", listed.getFirst().getString("selected_value"));

            try (AgtySqlCursor cursor = sql.openCursor(query)) {
                Assertions.assertEquals("value-2", cursor.next().getString("selected_value"));
            }
        } finally {
            dropTableQuietly(sql, table);
            sql.close();
        }
    }

    private static void recreateTable(AgtySQL sql, TestDatabaseProfile profile) {
        String table = profile.tableName();
        dropTableQuietly(sql, table);
        sql.clearErrors();

        sql.execute(profile.createTableDdl().replace("{table}", table));
        Assertions.assertFalse(sql.hasErrors(), sql.getErrors());
        Assertions.assertTrue(sql.tableIsExists(Arguments.builder().setTable(table)));
    }

    private static void insertRows(AgtySQL sql, String table) {
        for (int i = 1; i <= 4; i++) {
            long insertedId = sql.insert(
                    Arguments.builder()
                            .setTable(table)
                            .addData("id", i)
                            .addData("string", "value-" + i)
                            .addData("integers", i * 10)
                            .addData("bool", i % 2 == 0)
            );

            Assertions.assertEquals(0L, insertedId);
            Assertions.assertFalse(sql.hasErrors(), sql.getErrors());
        }
    }

    private static void dropTableQuietly(AgtySQL sql, String table) {
        try {
            sql.execute("DROP TABLE IF EXISTS " + table);
            sql.clearErrors();
        } catch (Exception ignored) {
            sql.clearErrors();
        }
    }
}
