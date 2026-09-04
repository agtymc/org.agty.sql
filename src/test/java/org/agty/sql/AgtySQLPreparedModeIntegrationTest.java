package org.agty.sql;

import org.agty.sql.data.Arguments;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.support.TestDatabaseProfile;
import org.agty.sql.support.TestDatabaseProfiles;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.stream.Stream;

@Tag("integration")
class AgtySQLPreparedModeIntegrationTest {

    private static Stream<TestDatabaseProfile> sqlProfiles() {
        return TestDatabaseProfiles.sqlProfiles();
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest(name = "prepared high-level CRUD: {0}")
    @MethodSource("sqlProfiles")
    void executesPreparedHighLevelCrud(TestDatabaseProfile profile) {
        AgtySQL sql = profile.createSql();
        String table = "{integration_prepared_" + profile.server() + "}";
        String originalValue = "O'Reilly & <admin>\\";

        try {
            recreateTable(sql, profile, table);

            sql.insert(Arguments.builder()
                    .useStatementPrepare(true)
                    .setTable(table)
                    .addDataInt("id", 1)
                    .addDataString("string", originalValue)
                    .addDataDecimal("integers", new BigDecimal("10"))
                    .addDataBoolean("bool", true));

            Assertions.assertFalse(sql.hasErrors(), sql.getErrors());

            SqlRow inserted = sql.fetch(Arguments.builder()
                    .useStatementPrepare(true)
                    .setTable(table)
                    .setWhere("[id] = ?", 1));

            Assertions.assertEquals(originalValue, inserted.getString("string"));

            Assertions.assertTrue(sql.update(Arguments.builder()
                    .useStatementPrepare(true)
                    .setTable(table)
                    .addDataString("string", "updated ' value")
                    .addDataDecimal("integers", new BigDecimal("20"))
                    .setWhere("[id] = ?", 1)));

            SqlRow updated = sql.fetch(Arguments.builder()
                    .useStatementPrepare(true)
                    .setQuery("SELECT * FROM " + table + " WHERE id = ?", 1));

            Assertions.assertEquals("updated ' value", updated.getString("string"));
            Assertions.assertEquals(20, updated.getInt("integers"));

            Assertions.assertTrue(sql.update(Arguments.builder()
                    .useStatementPrepare(true)
                    .setTable(table)
                    .addData("string", "")
                    .setWhere("[id] = ?", 1)));

            Assertions.assertEquals("", sql.fetch(Arguments.builder()
                    .useStatementPrepare(true)
                    .setTable(table)
                    .setWhere("[id] = ?", 1)).getString("string"));

            LinkedList<SqlRow> rows = sql.listArray(Arguments.builder()
                    .useStatementPrepare(true)
                    .setTable(table)
                    .setWhere("[id] >= ?", 1));

            Assertions.assertEquals(1, rows.size());
            Assertions.assertEquals(1L, sql.countRows(Arguments.builder()
                    .useStatementPrepare(true)
                    .setTable(table)
                    .setWhere("[id] = ?", 1)));

            Assertions.assertTrue(sql.rowIsExists(Arguments.builder()
                    .useStatementPrepare(true)
                    .setTable(table)
                    .setWhere("[id] = ?", 1)));
            Assertions.assertFalse(sql.rowIsExists(Arguments.builder()
                    .useStatementPrepare(true)
                    .setTable(table)
                    .setWhere("[string] = ?", "' OR 1=1 --")));
            Assertions.assertEquals(1L, sql.min(Arguments.builder()
                    .useStatementPrepare(true)
                    .setTable(table)
                    .setActionField("id")
                    .setWhere("[id] >= ?", 1)));
            Assertions.assertEquals(1L, sql.max(Arguments.builder()
                    .useStatementPrepare(true)
                    .setTable(table)
                    .setActionField("id")
                    .setWhere("[id] <= ?", 1)));

            try (AgtySqlCursor cursor = sql.openCursor(Arguments.builder()
                    .useStatementPrepare(true)
                    .setTable(table)
                    .setWhere("[id] = ?", 1))) {
                Assertions.assertEquals(1L, cursor.next().getLong("id"));
                Assertions.assertNull(cursor.next());
            }

            Assertions.assertTrue(sql.delete(Arguments.builder()
                    .useStatementPrepare(true)
                    .setTable(table)
                    .setWhere("[id] = ?", 1)));

            Assertions.assertEquals(0L, sql.countRows(Arguments.builder().setTable(table)));

            ArrayList<Arguments> batch = new ArrayList<>();
            batch.add(preparedRow(table, 2, "batch O'Reilly"));
            batch.add(preparedRow(table, 3, "[~literal"));
            sql.insert(batch);

            Assertions.assertEquals(2L, sql.countRows(Arguments.builder().setTable(table)));
            Assertions.assertEquals(
                    2L,
                    sql.getByField(table, "string", "batch O'Reilly").getLong("id")
            );
            Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> sql.getByField(table, "string] = '' OR 1=1 --", "ignored")
            );
            Assertions.assertEquals("[~literal", sql.fetch(Arguments.builder()
                    .useStatementPrepare(true)
                    .setTable(table)
                    .setWhere("[id] = ?", 3)).getString("string"));
        } finally {
            dropTableQuietly(sql, table);
            sql.close();
        }
    }

    private static Arguments preparedRow(String table, int id, String value) {
        return Arguments.builder()
                .useStatementPrepare(true)
                .setTable(table)
                .addDataInt("id", id)
                .addDataString("string", value)
                .addDataInt("integers", id)
                .addDataBoolean("bool", true);
    }

    private static void recreateTable(AgtySQL sql, TestDatabaseProfile profile, String table) {
        dropTableQuietly(sql, table);
        sql.clearErrors();
        sql.execute(profile.createTableDdl().replace("{table}", table));
        Assertions.assertFalse(sql.hasErrors(), sql.getErrors());
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
