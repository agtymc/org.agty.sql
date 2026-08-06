package org.agty.sql;

import org.agty.sql.dialect.DialectDriverRegistry;
import org.agty.sql.config.AgtySqlConfig;
import org.agty.sql.data.Arguments;
import org.agty.sql.driver.DialectCapabilities;
import org.agty.sql.driver.LastInsertIdStrategy;
import org.agty.sql.driver.UpdateAndGetStrategy;
import org.agty.sql.driver.WriteReturnStrategy;
import org.agty.sql.exceptions.AgtySqlException;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.support.TestDatabaseProfile;
import org.agty.sql.support.TestDatabaseProfiles;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class AgtySQLDialectCapabilitiesTest {

    private static Stream<TestDatabaseProfile> sqlProfiles() {
        return TestDatabaseProfiles.sqlProfiles();
    }

    @ParameterizedTest(name = "dialect capabilities: {0}")
    @MethodSource("sqlProfiles")
    void exposesExpectedCapabilities(TestDatabaseProfile profile) {
        AgtySQL sql = profile.createSql();

        try {
            DialectCapabilities actual = sql.getDialectCapabilities();
            Assertions.assertEquals(profile.capabilities(), actual);
            Assertions.assertEquals(
                    actual.insertAndGetStrategy() == WriteReturnStrategy.NATIVE_RETURNING,
                    actual.supportsInsertAndGetReturning()
            );
            Assertions.assertEquals(
                    actual.updateAndGetStrategy() == UpdateAndGetStrategy.NATIVE_RETURNING,
                    actual.supportsUpdateAndGetReturning()
            );
            Assertions.assertEquals(
                    actual.updateAndGetStrategy().usesFollowUpFetch(),
                    actual.usesFollowUpFetchForUpdateAndGet()
            );
        } finally {
            sql.close();
        }
    }

    @Test
    void registryUsesDialectImplementations() {
        Assertions.assertEquals(
                "org.agty.sql.dialect.mysql.MySQL",
                DialectDriverRegistry.getDialect("mysql", null).getClass().getName()
        );
        Assertions.assertEquals(
                "org.agty.sql.dialect.pgsql.PgSQL",
                DialectDriverRegistry.getDialect("pgsql", null).getClass().getName()
        );
        Assertions.assertEquals("mysql", DialectDriverRegistry.getDriverName("mysql"));
        Assertions.assertEquals("postgresql", DialectDriverRegistry.getDriverName("pgsql"));
    }

    @Test
    void updateAndGetFutureStrategiesExposeExpectedFlags() {
        DialectCapabilities primaryKeyFollowUp = DialectCapabilities.of(
                false,
                false,
                LastInsertIdStrategy.NONE,
                WriteReturnStrategy.NONE,
                UpdateAndGetStrategy.FOLLOW_UP_FETCH_BY_PRIMARY_KEY
        );
        DialectCapabilities unsafeFollowUp = DialectCapabilities.of(
                false,
                false,
                LastInsertIdStrategy.NONE,
                WriteReturnStrategy.NONE,
                UpdateAndGetStrategy.FOLLOW_UP_FETCH_UNSAFE
        );

        Assertions.assertTrue(primaryKeyFollowUp.supportsUpdateAndGet());
        Assertions.assertTrue(primaryKeyFollowUp.usesFollowUpFetchForUpdateAndGet());
        Assertions.assertTrue(primaryKeyFollowUp.usesPrimaryKeyFollowUpForUpdateAndGet());
        Assertions.assertFalse(primaryKeyFollowUp.usesUnsafeFollowUpForUpdateAndGet());

        Assertions.assertTrue(unsafeFollowUp.supportsUpdateAndGet());
        Assertions.assertTrue(unsafeFollowUp.usesFollowUpFetchForUpdateAndGet());
        Assertions.assertTrue(unsafeFollowUp.usesUnsafeFollowUpForUpdateAndGet());
        Assertions.assertTrue(unsafeFollowUp.updateAndGetStrategy().isCollisionProne());
    }

    @Test
    void legacyCompatibilityClassesOutsideAgtySqlWereRemoved() {
        Assertions.assertThrows(ClassNotFoundException.class, () -> Class.forName("org.agty.sql.factory.DriverSqlFactory"));
        Assertions.assertThrows(ClassNotFoundException.class, () -> Class.forName("org.agty.sql.factory.RowFactory"));
        Assertions.assertThrows(ClassNotFoundException.class, () -> Class.forName("org.agty.sql.base.Errors"));
        Assertions.assertThrows(ClassNotFoundException.class, () -> Class.forName("org.agty.sql.base.Logger"));
        Assertions.assertThrows(ClassNotFoundException.class, () -> Class.forName("org.agty.sql.base.SqlQuery"));
        Assertions.assertThrows(ClassNotFoundException.class, () -> Class.forName("org.agty.sql.utils.AgtySqlUtils"));
        Assertions.assertThrows(ClassNotFoundException.class, () -> Class.forName("org.agty.sql.driver.mysql.MySQL"));
        Assertions.assertThrows(ClassNotFoundException.class, () -> Class.forName("org.agty.sql.driver.pgsql.PgSQL"));
    }

    @Test
    void baseModelTypesRemainSupportedIn2x() {
        Assertions.assertFalse(org.agty.sql.base.Field.class.isAnnotationPresent(Deprecated.class));
        Assertions.assertFalse(org.agty.sql.base.FieldsType.class.isAnnotationPresent(Deprecated.class));
        Assertions.assertFalse(org.agty.sql.base.RowData.class.isAnnotationPresent(Deprecated.class));
        Assertions.assertFalse(org.agty.sql.base.RowDataEmpty.class.isAnnotationPresent(Deprecated.class));

        org.agty.sql.base.Field field = new org.agty.sql.base.Field("varchar", "CHARACTER VARYING", "255");
        Assertions.assertEquals("varchar", field.getAgtySqlType());
        Assertions.assertEquals("CHARACTER VARYING", field.getDriverSqlType());
        Assertions.assertEquals("255", field.getFieldLength());

        org.agty.sql.base.RowData row = new org.agty.sql.base.RowData();
        row.setData("id", 7).setData("name", "alice");
        Assertions.assertEquals(7, row.getInt("id"));
        Assertions.assertEquals("alice", row.getString("name"));
        Assertions.assertFalse(row.isEmpty());

        Assertions.assertTrue(org.agty.sql.support.RowFactory.newSqlRow() instanceof org.agty.sql.base.RowData);
        Assertions.assertTrue(org.agty.sql.support.RowFactory.emptyRow() instanceof org.agty.sql.base.RowDataEmpty);
    }

    @Test
    void primaryKeyFollowUpStrategyFailsFastWithoutPrimaryKeyMetadata() {
        AgtySQL sql = new FuturePrimaryKeyStrategySql();

        try {
            Assertions.assertThrows(
                    AgtySqlException.class,
                    () -> sql.updateAndGet(
                            Arguments.builder()
                                    .setTable("{test_table}")
                                    .setData("string", "after")
                                    .setWhere("[id] = %d", 1),
                            "id, string"
                    )
            );
        } finally {
            sql.close();
        }
    }

    private static final class FuturePrimaryKeyStrategySql extends AgtySQL {
        private static final DialectCapabilities CAPABILITIES = DialectCapabilities.of(
                false,
                false,
                LastInsertIdStrategy.NONE,
                WriteReturnStrategy.NONE,
                UpdateAndGetStrategy.FOLLOW_UP_FETCH_BY_PRIMARY_KEY
        );

        FuturePrimaryKeyStrategySql() {
            super(new AgtySqlConfig()
                    .setDriver("mysql")
                    .setThrowException(true)
                    .setDebug(false));
        }

        @Override
        public DialectCapabilities getDialectCapabilities() {
            return CAPABILITIES;
        }
    }
}
