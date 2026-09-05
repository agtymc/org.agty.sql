package org.agty.sql;

import org.agty.sql.dialect.DialectDriverRegistry;
import org.agty.sql.config.AgtySqlConfig;
import org.agty.sql.data.Arguments;
import org.agty.sql.driver.DialectCapabilities;
import org.agty.sql.driver.LastInsertIdStrategy;
import org.agty.sql.driver.ReadAfterWriteSafety;
import org.agty.sql.driver.UpdateAndGetStrategy;
import org.agty.sql.driver.WriteReturnStrategy;
import org.agty.sql.exceptions.AgtySqlException;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.support.TestDatabaseProfile;
import org.agty.sql.support.TestDatabaseProfiles;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@Tag("integration")
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
        Assertions.assertEquals(
                "org.agty.sql.dialect.mssql.MsSQL",
                DialectDriverRegistry.getDialect("mssql", null).getClass().getName()
        );
        Assertions.assertEquals("mysql", DialectDriverRegistry.getDriverName("mysql"));
        Assertions.assertEquals("postgresql", DialectDriverRegistry.getDriverName("pgsql"));
        Assertions.assertEquals("sqlserver", DialectDriverRegistry.getDriverName("mssql"));
    }

    @Test
    void mssqlDialectExposesNativeReturningCapabilities() {
        DialectCapabilities capabilities = DialectDriverRegistry.getDialect("mssql", null).getCapabilities();

        Assertions.assertEquals(
                DialectCapabilities.of(
                        false,
                        true,
                        LastInsertIdStrategy.CONNECTION_FUNCTION,
                        WriteReturnStrategy.NATIVE_RETURNING,
                        UpdateAndGetStrategy.NATIVE_RETURNING
                ),
                capabilities
        );
        Assertions.assertTrue(capabilities.supportsInsertAndGetReturning());
        Assertions.assertTrue(capabilities.supportsUpdateAndGetReturning());
        Assertions.assertTrue(capabilities.supportsLastInsertId());
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
    void exposesExplicitReadAfterWriteSafetyLevels() {
        DialectCapabilities postgresql = DialectDriverRegistry
                .getDialect("pgsql", null)
                .getCapabilities();
        DialectCapabilities mysql = DialectDriverRegistry
                .getDialect("mysql", null)
                .getCapabilities();
        DialectCapabilities mariadb = DialectDriverRegistry
                .getDialect("mariadb", null)
                .getCapabilities();
        DialectCapabilities sqlite = DialectDriverRegistry
                .getDialect("sqlite", null)
                .getCapabilities();
        DialectCapabilities h2 = DialectDriverRegistry
                .getDialect("h2", null)
                .getCapabilities();
        DialectCapabilities mssql = DialectDriverRegistry
                .getDialect("mssql", null)
                .getCapabilities();

        Assertions.assertEquals(ReadAfterWriteSafety.ATOMIC, postgresql.insertAndGetSafety());
        Assertions.assertEquals(ReadAfterWriteSafety.ATOMIC, postgresql.updateAndGetSafety());
        Assertions.assertEquals(ReadAfterWriteSafety.CONNECTION_SCOPED, postgresql.lastInsertIdSafety());

        Assertions.assertEquals(ReadAfterWriteSafety.TRANSACTION_GUARDED, mysql.insertAndGetSafety());
        Assertions.assertEquals(ReadAfterWriteSafety.COLLISION_PRONE, mysql.updateAndGetSafety());
        Assertions.assertTrue(mysql.insertAndGetSafety().requiresTransaction());

        Assertions.assertEquals(ReadAfterWriteSafety.TRANSACTION_GUARDED, mariadb.insertAndGetSafety());
        Assertions.assertEquals(ReadAfterWriteSafety.COLLISION_PRONE, mariadb.updateAndGetSafety());
        Assertions.assertEquals(ReadAfterWriteSafety.CONNECTION_SCOPED, mariadb.lastInsertIdSafety());

        Assertions.assertEquals(ReadAfterWriteSafety.TRANSACTION_GUARDED, sqlite.insertAndGetSafety());
        Assertions.assertEquals(ReadAfterWriteSafety.COLLISION_PRONE, sqlite.updateAndGetSafety());
        Assertions.assertEquals(ReadAfterWriteSafety.CONNECTION_SCOPED, sqlite.lastInsertIdSafety());

        Assertions.assertEquals(ReadAfterWriteSafety.COLLISION_PRONE, h2.insertAndGetSafety());
        Assertions.assertTrue(h2.updateAndGetSafety().isCollisionProne());

        Assertions.assertEquals(ReadAfterWriteSafety.ATOMIC, mssql.insertAndGetSafety());
        Assertions.assertEquals(ReadAfterWriteSafety.ATOMIC, mssql.updateAndGetSafety());
        Assertions.assertEquals(ReadAfterWriteSafety.CONNECTION_SCOPED, mssql.lastInsertIdSafety());
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
                                    .addData("string", "after")
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
