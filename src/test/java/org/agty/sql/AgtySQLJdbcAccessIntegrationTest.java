package org.agty.sql;

import org.agty.sql.data.Arguments;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.support.TestDatabaseProfile;
import org.agty.sql.support.TestDatabaseProfiles;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Stream;

@Tag("integration")
class AgtySQLJdbcAccessIntegrationTest {

    private static Stream<TestDatabaseProfile> sqlProfiles() {
        return TestDatabaseProfiles.sqlProfiles();
    }

    @ParameterizedTest(name = "jdbc access: {0}")
    @MethodSource("sqlProfiles")
    void exposesLowLevelJdbcApi(TestDatabaseProfile profile) throws Exception {
        AgtySQL sql = profile.createSql();
        String table = "{integration_jdbc_access_" + profile.server() + "}";

        try {
            recreateTable(sql, profile, table);

            Connection connection = sql.getConnection();
            Assertions.assertNotNull(connection);
            Assertions.assertTrue(connection.isValid(2) || connection.isClosed() == false);

            sql.setFetchSize(7);
            Assertions.assertEquals(7, sql.getFetchSize());

            try (Statement statement = sql.getStatement()) {
                Assertions.assertNotNull(statement);
                Assertions.assertEquals(7, statement.getFetchSize());
            }

            Assertions.assertTrue(sql.isAutoCommit());
            sql.beginTransaction();
            Assertions.assertFalse(sql.isAutoCommit());
            sql.rollback();
            sql.setAutoCommit(true);
            Assertions.assertTrue(sql.isAutoCommit());

            try (PreparedStatement preparedStatement = sql.prepareStatement(
                    "INSERT INTO {integration_jdbc_access_" + profile.server() + "} (id, string, integers, bool) VALUES (?, ?, ?, ?)",
                    false
            )) {
                preparedStatement.setLong(1, 101L);
                preparedStatement.setString(2, "jdbc");
                preparedStatement.setInt(3, 55);
                preparedStatement.setBoolean(4, true);
                Assertions.assertEquals(1, preparedStatement.executeUpdate());
            }

            SqlRow inserted = sql.fetch(Arguments.builder().setTable(table).setWhere("[id] = %d", 101));
            Assertions.assertEquals("jdbc", inserted.getString("string"));
            Assertions.assertEquals(55, inserted.getInt("integers"));

            try (PreparedStatement preparedStatement = sql.prepareStatement("SELECT 1 AS test_number", true);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                Assertions.assertTrue(resultSet.next());
                Assertions.assertEquals(1, resultSet.getInt("test_number"));
            }
        } finally {
            dropTableQuietly(sql, table);
            sql.close();
        }
    }

    @ParameterizedTest(name = "jdbc batch: {0}")
    @MethodSource("sqlProfiles")
    void executesBatchQueries(TestDatabaseProfile profile) {
        AgtySQL sql = profile.createSql();
        String table = "{integration_jdbc_batch_" + profile.server() + "}";

        try {
            recreateTable(sql, profile, table);

            int[] result = sql.executeBatch(List.of(
                    "INSERT INTO " + table + " (id, string, integers, bool) VALUES (201, 'batch-1', 11, " + profile.sqlBooleanLiteral(true) + ")",
                    "INSERT INTO " + table + " (id, string, integers, bool) VALUES (202, 'batch-2', 22, " + profile.sqlBooleanLiteral(false) + ")"
            ));

            Assertions.assertEquals(2, result.length);
            Assertions.assertEquals(2L, sql.countRows(Arguments.builder().setTable(table)));
            Assertions.assertEquals("batch-1", sql.fetch(Arguments.builder().setTable(table).setWhere("[id] = %d", 201)).getString("string"));
            Assertions.assertEquals("batch-2", sql.fetch(Arguments.builder().setTable(table).setWhere("[id] = %d", 202)).getString("string"));
        } finally {
            dropTableQuietly(sql, table);
            sql.close();
        }
    }

    @ParameterizedTest(name = "jdbc generated keys: {0}")
    @MethodSource("sqlProfiles")
    void readsGeneratedKeysFromPreparedStatement(TestDatabaseProfile profile) throws Exception {
        AgtySQL sql = profile.createSql();
        String table = "{integration_jdbc_keys_" + profile.server() + "}";

        try {
            recreateAutoIdTable(sql, table, profile);

            try (PreparedStatement preparedStatement = sql.prepareStatement(
                    "INSERT INTO " + table + " (string) VALUES (?)",
                    Statement.RETURN_GENERATED_KEYS,
                    false
            )) {
                preparedStatement.setString(1, "generated-by-jdbc");
                Assertions.assertEquals(1, preparedStatement.executeUpdate());

                SqlRow keys = sql.getGeneratedKeys(preparedStatement);
                Long generatedId = firstGeneratedKey(keys);

                Assertions.assertNotNull(generatedId);
                Assertions.assertTrue(generatedId >= 1L);

                SqlRow inserted = sql.fetch(Arguments.builder().setTable(table).setWhere("[id] = %d", generatedId));
                Assertions.assertEquals("generated-by-jdbc", inserted.getString("string"));
            }
        } finally {
            dropTableQuietly(sql, table);
            sql.close();
        }
    }

    private static void recreateTable(AgtySQL sql, TestDatabaseProfile profile, String table) {
        dropTableQuietly(sql, table);
        sql.clearErrors();
        sql.execute(profile.createTableDdl().replace("{table}", table));
        Assertions.assertFalse(sql.hasErrors(), sql.getErrors());
        Assertions.assertTrue(sql.tableIsExists(Arguments.builder().setTable(table)));
    }

    private static void recreateAutoIdTable(AgtySQL sql, String table, TestDatabaseProfile profile) {
        dropTableQuietly(sql, table);
        sql.clearErrors();
        sql.execute(profile.createAutoIdTableDdl().replace("{table}", table));
        Assertions.assertFalse(sql.hasErrors(), sql.getErrors());
        Assertions.assertTrue(sql.tableIsExists(Arguments.builder().setTable(table)));
    }

    private static Long firstGeneratedKey(SqlRow keys) {
        if (keys.isEmpty()) {
            return null;
        }

        Object value = keys.getObject("GENERATED_KEY");
        if (value == null) {
            value = keys.getObject("ID");
        }
        if (value == null) {
            value = keys.getObject("id");
        }
        if (value == null && keys instanceof java.util.Map<?, ?> map && !map.isEmpty()) {
            value = map.values().iterator().next();
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        return value == null ? null : Long.parseLong(value.toString());
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
