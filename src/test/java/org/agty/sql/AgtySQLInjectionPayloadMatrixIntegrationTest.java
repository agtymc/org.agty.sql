package org.agty.sql;

import org.agty.sql.data.Arguments;
import org.agty.sql.data.SqlExpression;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.support.TestDatabaseProfile;
import org.agty.sql.support.TestDatabaseProfiles;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

@Tag("integration")
class AgtySQLInjectionPayloadMatrixIntegrationTest {
    private static final String INJECTION_PAYLOAD = "' OR 1=1 --";
    private static final List<String> TEXT_MATRIX = List.of(
            "'",
            "\"",
            "&",
            "\\",
            "line one\nline two",
            "Привет 世界",
            ""
    );

    private static Stream<TestDatabaseProfile> sqlProfiles() {
        return TestDatabaseProfiles.sqlProfiles();
    }

    @ParameterizedTest(name = "prepared injection matrix: {0}")
    @MethodSource("sqlProfiles")
    void preparedValuesRemainDataAcrossCrud(TestDatabaseProfile profile) {
        AgtySQL sql = profile.createSql();
        String table = "{security_prepared_" + profile.server() + "}";

        try {
            recreateTable(sql, profile, table);
            int id = 1;
            for (String value : TEXT_MATRIX) {
                insertPrepared(sql, table, id, value);
                SqlRow row = sql.fetch(Arguments.builder()
                        .useStatementPrepare(true)
                        .setTable(table)
                        .setWhere("[string] = ?", value));
                Assertions.assertEquals(value, row.getString("string"));
                id++;
            }

            sql.insert(Arguments.builder()
                    .useStatementPrepare(true)
                    .setTable(table)
                    .addDataInt("id", id)
                    .addDataNull("string")
                    .addDataInt("integers", 0)
                    .addDataBoolean("bool", false));
            SqlRow nullRow = sql.fetch(Arguments.builder()
                    .useStatementPrepare(true)
                    .setTable(table)
                    .setWhere(SqlExpression.trusted("[string] IS NULL")));
            Assertions.assertEquals(id, nullRow.getInt("id"));

            int payloadId = 900;
            int controlId = 901;
            insertPrepared(sql, table, payloadId, "initial");
            insertPrepared(sql, table, controlId, "control");

            Assertions.assertTrue(sql.update(Arguments.builder()
                    .useStatementPrepare(true)
                    .setTable(table)
                    .addDataString("string", INJECTION_PAYLOAD)
                    .setWhere("[id] = ?", payloadId)));
            Assertions.assertEquals(payloadId, sql.fetch(Arguments.builder()
                    .useStatementPrepare(true)
                    .setTable(table)
                    .setWhere("[string] = ?", INJECTION_PAYLOAD)).getInt("id"));

            Assertions.assertTrue(sql.update(Arguments.builder()
                    .useStatementPrepare(true)
                    .setTable(table)
                    .addDataInt("integers", 99)
                    .setWhere("[string] = ?", INJECTION_PAYLOAD)));
            Assertions.assertEquals(99, fetchById(sql, table, payloadId).getInt("integers"));
            Assertions.assertEquals(controlId, fetchById(sql, table, controlId).getInt("integers"));

            Assertions.assertTrue(sql.delete(Arguments.builder()
                    .useStatementPrepare(true)
                    .setTable(table)
                    .setWhere("[string] = ?", INJECTION_PAYLOAD)));
            Assertions.assertFalse(sql.rowIsExists(Arguments.builder()
                    .useStatementPrepare(true)
                    .setTable(table)
                    .setWhere("[id] = ?", payloadId)));
            Assertions.assertTrue(sql.rowIsExists(Arguments.builder()
                    .useStatementPrepare(true)
                    .setTable(table)
                    .setWhere("[id] = ?", controlId)));
        } finally {
            dropTableQuietly(sql, table);
            sql.close();
        }
    }

    @ParameterizedTest(name = "legacy injection matrix: {0}")
    @MethodSource("sqlProfiles")
    void legacyEncodingNeutralizesPayloadAcrossCrud(TestDatabaseProfile profile) {
        AgtySQL sql = profile.createSql();
        String table = "{security_legacy_" + profile.server() + "}";

        try {
            recreateTable(sql, profile, table);
            int id = 1;
            for (String value : TEXT_MATRIX.stream().filter(item -> !item.isEmpty()).toList()) {
                insertLegacy(sql, table, id, value);
                SqlRow row = sql.fetch(Arguments.builder()
                        .setTable(table)
                        .setWhere("[string] = '%s'", value));
                Assertions.assertEquals(value, row.getDstring("string"));
                id++;
            }

            int payloadId = 900;
            int controlId = 901;
            insertLegacy(sql, table, payloadId, INJECTION_PAYLOAD);
            insertLegacy(sql, table, controlId, "control");

            Assertions.assertEquals(payloadId, sql.fetch(Arguments.builder()
                    .setTable(table)
                    .setWhere("[string] = '%s'", INJECTION_PAYLOAD)).getInt("id"));

            Assertions.assertTrue(sql.update(Arguments.builder()
                    .setTable(table)
                    .addDataInt("integers", 99)
                    .setWhere("[string] = '%s'", INJECTION_PAYLOAD)));
            Assertions.assertEquals(99, fetchById(sql, table, payloadId).getInt("integers"));
            Assertions.assertEquals(controlId, fetchById(sql, table, controlId).getInt("integers"));

            Assertions.assertTrue(sql.delete(Arguments.builder()
                    .setTable(table)
                    .setWhere("[string] = '%s'", INJECTION_PAYLOAD)));
            Assertions.assertFalse(sql.rowIsExists(
                    Arguments.builder().setTable(table).setWhere("[id] = %d", payloadId)
            ));
            Assertions.assertTrue(sql.rowIsExists(
                    Arguments.builder().setTable(table).setWhere("[id] = %d", controlId)
            ));
        } finally {
            dropTableQuietly(sql, table);
            sql.close();
        }
    }

    private static void insertPrepared(AgtySQL sql, String table, int id, String value) {
        sql.insert(Arguments.builder()
                .useStatementPrepare(true)
                .setTable(table)
                .addDataInt("id", id)
                .addDataString("string", value)
                .addDataInt("integers", id)
                .addDataBoolean("bool", true));
        Assertions.assertFalse(sql.hasErrors(), sql.getErrors());
    }

    private static void insertLegacy(AgtySQL sql, String table, int id, String value) {
        sql.insert(Arguments.builder()
                .setTable(table)
                .addDataInt("id", id)
                .addDataString("string", value)
                .addDataInt("integers", id)
                .addDataBoolean("bool", true));
        Assertions.assertFalse(sql.hasErrors(), sql.getErrors());
    }

    private static SqlRow fetchById(AgtySQL sql, String table, int id) {
        return sql.fetch(Arguments.builder()
                .useStatementPrepare(true)
                .setTable(table)
                .setWhere("[id] = ?", id));
    }

    private static void recreateTable(AgtySQL sql, TestDatabaseProfile profile, String table) {
        dropTableQuietly(sql, table);
        sql.clearErrors();
        String ddl = profile.createTableDdl().replace("{table}", table);
        if ("mssql".equals(profile.server())) {
            ddl = ddl.replace("VARCHAR(255)", "NVARCHAR(255)");
        }
        sql.execute(ddl);
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
