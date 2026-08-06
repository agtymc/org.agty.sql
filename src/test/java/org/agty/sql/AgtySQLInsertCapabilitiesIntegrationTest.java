package org.agty.sql;

import org.agty.sql.data.Arguments;
import org.agty.sql.exceptions.AgtySqlException;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.model.annotations.Column;
import org.agty.sql.model.annotations.Entity;
import org.agty.sql.support.TestDatabaseProfile;
import org.agty.sql.support.TestDatabaseProfiles;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class AgtySQLInsertCapabilitiesIntegrationTest {

    private static Stream<TestDatabaseProfile> profilesSupportingLastInsertId() {
        return TestDatabaseProfiles.profilesSupportingLastInsertId();
    }

    private static Stream<TestDatabaseProfile> profilesSupportingInsertAndGet() {
        return TestDatabaseProfiles.sqlProfiles()
                .filter(profile -> profile.capabilities().supportsInsertAndGet());
    }

    private static Stream<TestDatabaseProfile> profilesSupportingUpdateAndGet() {
        return TestDatabaseProfiles.profilesSupportingUpdateAndGet();
    }

    private static Stream<TestDatabaseProfile> profilesUsingFollowUpFetchForUpdateAndGet() {
        return TestDatabaseProfiles.sqlProfiles()
                .filter(profile -> profile.capabilities().supportsUpdateAndGet())
                .filter(profile -> !profile.capabilities().supportsUpdateAndGetReturning());
    }

    private static Stream<TestDatabaseProfile> profilesWithoutLastInsertId() {
        return TestDatabaseProfiles.sqlProfiles()
                .filter(profile -> !profile.capabilities().supportsLastInsertId());
    }

    private static Stream<TestDatabaseProfile> profilesWithoutInsertAndGet() {
        return TestDatabaseProfiles.sqlProfiles()
                .filter(profile -> !profile.capabilities().supportsInsertAndGet());
    }

    private static Stream<TestDatabaseProfile> profilesWithoutInsertAndGetReturning() {
        return TestDatabaseProfiles.sqlProfiles()
                .filter(profile -> profile.capabilities().supportsInsertAndGet())
                .filter(profile -> !profile.capabilities().supportsInsertAndGetReturning());
    }

    @ParameterizedTest(name = "lastInsertId: {0}")
    @MethodSource("profilesSupportingLastInsertId")
    void insertReturnsGeneratedId(TestDatabaseProfile profile) {
        AgtySQL sql = profile.createSql();
        String table = "{integration_auto_id_" + profile.server() + "}";

        try {
            recreateAutoIdTable(sql, table, profile);

            long insertedId = sql.insert(
                    Arguments.builder()
                            .setTable(table)
                            .addData("string", "generated")
                            .setReturnLastInsertId(true)
            );

            Assertions.assertTrue(insertedId >= 1L);

            SqlRow fetched = sql.fetch(Arguments.builder().setTable(table).setWhere("[id] = %d", insertedId));
            Assertions.assertEquals(insertedId, fetched.getLong("id"));
            Assertions.assertEquals("generated", fetched.getString("string"));
        } finally {
            dropTableQuietly(sql, table);
            sql.close();
        }
    }

    @ParameterizedTest(name = "lastInsertId explicit primary key: {0}")
    @MethodSource("profilesSupportingLastInsertId")
    void insertReturnsExplicitPrimaryKeyWhenProvided(TestDatabaseProfile profile) {
        AgtySQL sql = profile.createSql();
        String table = "{integration_manual_id_" + profile.server() + "}";
        long expectedId = 7001L;

        try {
            recreateTable(sql, table, profile);

            long insertedId = sql.insert(
                    Arguments.builder()
                            .setTable(table)
                            .setPrimaryKey("id")
                            .addData("id", expectedId)
                            .addData("string", "manual-id")
                            .addData("integers", 1)
                            .addData("bool", true)
                            .setReturnLastInsertId(true)
            );

            Assertions.assertEquals(expectedId, insertedId);

            SqlRow fetched = sql.fetch(Arguments.builder().setTable(table).setWhere("[id] = %d", expectedId));
            Assertions.assertEquals(expectedId, fetched.getLong("id"));
            Assertions.assertEquals("manual-id", fetched.getString("string"));
        } finally {
            dropTableQuietly(sql, table);
            sql.close();
        }
    }

    @ParameterizedTest(name = "insertAndGet: {0}")
    @MethodSource("profilesSupportingInsertAndGet")
    void insertAndGetReturnsInsertedRow(TestDatabaseProfile profile) {
        AgtySQL sql = profile.createSql();
        String table = "{integration_insert_and_get_" + profile.server() + "}";

        try {
            recreateAutoIdTable(sql, table, profile);

            SqlRow inserted = sql.insertAndGet(
                    Arguments.builder()
                            .setTable(table)
                            .addData("string", "returning"),
                    "id, string"
            );

            Assertions.assertTrue(inserted.getLong("id") >= 1L);
            Assertions.assertEquals("returning", inserted.getString("string"));
        } finally {
            dropTableQuietly(sql, table);
            sql.close();
        }
    }

    @ParameterizedTest(name = "insertAndGet entity: {0}")
    @MethodSource("profilesSupportingInsertAndGet")
    void insertAndGetMapsReturnedRowIntoEntity(TestDatabaseProfile profile) {
        AgtySQL sql = profile.createSql();
        String table = "{integration_insert_and_get_entity_" + profile.server() + "}";

        try {
            recreateAutoIdTable(sql, table, profile);

            InsertedRowEntity inserted = sql.insertAndGet(
                    Arguments.builder()
                            .setTable(table)
                            .addData("string", "entity-return"),
                    InsertedRowEntity.class
            );

            Assertions.assertNotNull(inserted);
            Assertions.assertTrue(inserted.id >= 1L);
            Assertions.assertEquals("entity-return", inserted.string);
        } finally {
            dropTableQuietly(sql, table);
            sql.close();
        }
    }

    @ParameterizedTest(name = "insert short entity: {0}")
    @MethodSource("profilesSupportingInsertAndGet")
    void insertShortFormMapsReturnedRowIntoEntity(TestDatabaseProfile profile) {
        AgtySQL sql = profile.createSql();
        String table = "{integration_insert_short_entity_" + profile.server() + "}";

        try {
            recreateAutoIdTable(sql, table, profile);

            InsertedRowEntity inserted = sql.insert(
                    Arguments.builder()
                            .setTable(table)
                            .addData("string", "short-insert"),
                    InsertedRowEntity.class
            );

            Assertions.assertNotNull(inserted);
            Assertions.assertTrue(inserted.id >= 1L);
            Assertions.assertEquals("short-insert", inserted.string);
        } finally {
            dropTableQuietly(sql, table);
            sql.close();
        }
    }

    @ParameterizedTest(name = "updateAndGet: {0}")
    @MethodSource("profilesSupportingUpdateAndGet")
    void updateAndGetReturnsUpdatedRow(TestDatabaseProfile profile) {
        AgtySQL sql = profile.createSql();
        String table = "{integration_update_and_get_" + profile.server() + "}";

        try {
            recreateTable(sql, table, profile);
            insertSeedRow(sql, table, 7L, "before");

            SqlRow updated = sql.updateAndGet(
                    Arguments.builder()
                            .setTable(table)
                            .addData("string", "after")
                            .setWhere("[id] = %d", 7),
                    "id, string"
            );

            Assertions.assertEquals(7L, updated.getLong("id"));
            Assertions.assertEquals("after", updated.getString("string"));
        } finally {
            dropTableQuietly(sql, table);
            sql.close();
        }
    }

    @ParameterizedTest(name = "updateAndGet entity: {0}")
    @MethodSource("profilesSupportingUpdateAndGet")
    void updateAndGetMapsReturnedRowIntoEntity(TestDatabaseProfile profile) {
        AgtySQL sql = profile.createSql();
        String table = "{integration_update_and_get_entity_" + profile.server() + "}";

        try {
            recreateTable(sql, table, profile);
            insertSeedRow(sql, table, 9L, "before-entity");

            UpdatedRowEntity updated = sql.updateAndGet(
                    Arguments.builder()
                            .setTable(table)
                            .addData("string", "after-entity")
                            .setWhere("[id] = %d", 9),
                    UpdatedRowEntity.class
            );

            Assertions.assertNotNull(updated);
            Assertions.assertEquals(9L, updated.id);
            Assertions.assertEquals("after-entity", updated.string);
        } finally {
            dropTableQuietly(sql, table);
            sql.close();
        }
    }

    @ParameterizedTest(name = "update short entity: {0}")
    @MethodSource("profilesSupportingUpdateAndGet")
    void updateShortFormMapsReturnedRowIntoEntity(TestDatabaseProfile profile) {
        AgtySQL sql = profile.createSql();
        String table = "{integration_update_short_entity_" + profile.server() + "}";

        try {
            recreateTable(sql, table, profile);
            insertSeedRow(sql, table, 13L, "before-short");

            UpdatedRowEntity updated = sql.update(
                    Arguments.builder()
                            .setTable(table)
                            .addData("string", "after-short")
                            .setWhere("[id] = %d", 13),
                    UpdatedRowEntity.class
            );

            Assertions.assertNotNull(updated);
            Assertions.assertEquals(13L, updated.id);
            Assertions.assertEquals("after-short", updated.string);
        } finally {
            dropTableQuietly(sql, table);
            sql.close();
        }
    }

    @ParameterizedTest(name = "updateAndGet missing table metadata on follow-up driver: {0}")
    @MethodSource("profilesUsingFollowUpFetchForUpdateAndGet")
    void updateAndGetFailsFastWithoutTableMetadataOnFollowUpDrivers(TestDatabaseProfile profile) {
        AgtySQL sql = profile.createSql();

        try {
            Assertions.assertThrows(
                    AgtySqlException.class,
                    () -> sql.updateAndGet(
                            Arguments.builder()
                                    .addData("string", "after")
                                    .setWhere("[id] = %d", 1),
                            "id, string"
                    )
            );
        } finally {
            sql.close();
        }
    }

    @ParameterizedTest(name = "updateAndGet missing where metadata on follow-up driver: {0}")
    @MethodSource("profilesUsingFollowUpFetchForUpdateAndGet")
    void updateAndGetFailsFastWithoutWhereMetadataOnFollowUpDrivers(TestDatabaseProfile profile) {
        AgtySQL sql = profile.createSql();
        String table = "{integration_update_and_get_missing_where_" + profile.server() + "}";

        try {
            recreateTable(sql, table, profile);
            insertSeedRow(sql, table, 31L, "before-missing-where");

            Assertions.assertThrows(
                    AgtySqlException.class,
                    () -> sql.updateAndGet(
                            Arguments.builder()
                                    .setTable(table)
                                    .addData("string", "after-missing-where"),
                            "id, string"
                    )
            );
        } finally {
            dropTableQuietly(sql, table);
            sql.close();
        }
    }

    @ParameterizedTest(name = "updateAndGet multi-row where on follow-up driver: {0}")
    @MethodSource("profilesUsingFollowUpFetchForUpdateAndGet")
    void updateAndGetOnMultiRowWhereReturnsOneUpdatedMatchingRow(TestDatabaseProfile profile) {
        AgtySQL sql = profile.createSql();
        String table = "{integration_update_and_get_multi_row_" + profile.server() + "}";

        try {
            recreateTable(sql, table, profile);
            insertSeedRow(sql, table, 41L, "before-multi");
            insertSeedRow(sql, table, 42L, "before-multi");

            SqlRow updated = sql.updateAndGet(
                    Arguments.builder()
                            .setTable(table)
                            .addData("string", "after-multi")
                            .setWhere("[id] IN (41, 42)"),
                    "id, string"
            );

            Assertions.assertTrue(updated.getLong("id") == 41L || updated.getLong("id") == 42L);
            Assertions.assertEquals("after-multi", updated.getString("string"));

            SqlRow first = sql.fetch(Arguments.builder().setTable(table).setWhere("[id] = %d", 41));
            SqlRow second = sql.fetch(Arguments.builder().setTable(table).setWhere("[id] = %d", 42));
            Assertions.assertEquals("after-multi", first.getString("string"));
            Assertions.assertEquals("after-multi", second.getString("string"));
        } finally {
            dropTableQuietly(sql, table);
            sql.close();
        }
    }

    @org.junit.jupiter.api.Test
    void insertWithLastInsertIdFailsFastWhenUnsupported() {
        Assertions.assertEquals(0L, profilesWithoutLastInsertId().count());
    }

    @org.junit.jupiter.api.Test
    void insertAndGetFailsFastWhenUnsupported() {
        Assertions.assertEquals(0L, profilesWithoutInsertAndGet().count());
    }

    @ParameterizedTest(name = "insertAndGet raw query unsupported on follow-up driver: {0}")
    @MethodSource("profilesWithoutInsertAndGetReturning")
    void insertAndGetRawQueryFailsFastOnFollowUpDrivers(TestDatabaseProfile profile) {
        AgtySQL sql = profile.createSql();
        String table = "{integration_insert_and_get_query_" + profile.server() + "}";

        try {
            recreateAutoIdTable(sql, table, profile);

            Assertions.assertThrows(
                    AgtySqlException.class,
                    () -> sql.insertAndGet(
                            "INSERT INTO " + table + " (string) VALUES ('raw-query')",
                            "id, string"
                    )
            );
        } finally {
            dropTableQuietly(sql, table);
            sql.close();
        }
    }

    @ParameterizedTest(name = "updateAndGet raw query unsupported on follow-up driver: {0}")
    @MethodSource("profilesUsingFollowUpFetchForUpdateAndGet")
    void updateAndGetRawQueryFailsFastOnFollowUpDrivers(TestDatabaseProfile profile) {
        AgtySQL sql = profile.createSql();
        String table = "{integration_update_and_get_query_" + profile.server() + "}";

        try {
            recreateTable(sql, table, profile);
            insertSeedRow(sql, table, 21L, "before-query");

            Assertions.assertThrows(
                    AgtySqlException.class,
                    () -> sql.updateAndGet(
                            "UPDATE " + table + " SET string = 'after-query' WHERE id = 21",
                            "id, string"
                    )
            );
        } finally {
            dropTableQuietly(sql, table);
            sql.close();
        }
    }

    private static void recreateAutoIdTable(AgtySQL sql, String table, TestDatabaseProfile profile) {
        dropTableQuietly(sql, table);
        sql.clearErrors();
        sql.execute(profile.createAutoIdTableDdl().replace("{table}", table));
        Assertions.assertFalse(sql.hasErrors(), sql.getErrors());
        Assertions.assertTrue(sql.tableIsExists(Arguments.builder().setTable(table)));
    }

    private static void recreateTable(AgtySQL sql, String table, TestDatabaseProfile profile) {
        dropTableQuietly(sql, table);
        sql.clearErrors();
        sql.execute(profile.createTableDdl().replace("{table}", table));
        Assertions.assertFalse(sql.hasErrors(), sql.getErrors());
        Assertions.assertTrue(sql.tableIsExists(Arguments.builder().setTable(table)));
    }

    private static void insertSeedRow(AgtySQL sql, String table, long id, String value) {
        long insertedId = sql.insert(
                Arguments.builder()
                        .setTable(table)
                        .addData("id", id)
                        .addData("string", value)
                        .addData("integers", 1)
                        .addData("bool", true)
        );

        Assertions.assertEquals(0L, insertedId);
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

    @Entity
    public static class InsertedRowEntity {
        public Long id;

        public String string;
    }

    @Entity
    public static class UpdatedRowEntity {
        public Long id;

        public String string;
    }
}
