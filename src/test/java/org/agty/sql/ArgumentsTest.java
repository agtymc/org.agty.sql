package org.agty.sql;

import org.agty.sql.data.Arguments;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentsTest {

    @Test
    void formatsWhereClause() {
        Arguments arguments = Arguments.builder()
                .setWhere("[table] = '%s' AND id = %d", "my_table", 1);

        assertEquals("[table] = 'my_table' AND id = 1", arguments.getWhere());
    }

    @Test
    void storesDataAndColumns() {
        Arguments arguments = Arguments.builder()
                .setTable("{users}")
                .setActionField("id")
                .addData("name", "alice")
                .addData("age", 42)
                .addColumn("id")
                .addColumn("name");

        assertEquals("{users}", arguments.getTable());
        assertEquals("id", arguments.getActionField());
        assertEquals("alice", arguments.getData("name"));
        assertEquals(42, arguments.getData("age"));
        assertEquals(2, arguments.dataSize());
        assertEquals(2, arguments.getColumns().size());
        assertTrue(arguments.hasData());
        assertTrue(arguments.hasColumns());
        assertTrue(arguments.hasActionField());
    }

    @Test
    void keepsFlagsExplicit() {
        Arguments arguments = Arguments.builder()
                .setNoStringEncode(true)
                .setNoRebuildQuery(true)
                .setForceRebuildQuery(true)
                .setReturnLastInsertId(true);

        assertTrue(arguments.noStringEncode());
        assertTrue(arguments.noRebuildQuery());
        assertTrue(arguments.forceRebuildQuery());
        assertTrue(arguments.returnLastInsertId());
    }

    @Test
    void convertsBooleanValuesByDriver() {
        Arguments arguments = Arguments.builder();

        assertEquals(1, arguments.getBooleanValueForDriver(true, "mysql"));
        assertEquals(0, arguments.getBooleanValueForDriver(false, "mariadb"));
        assertEquals(1, arguments.getBooleanValueForDriver(true, "mssql"));
        assertEquals(0, arguments.getBooleanValueForDriver(false, "sqlite"));
        assertEquals(1, arguments.getBooleanValueForDriver(true, "h2"));
        assertEquals(true, arguments.getBooleanValueForDriver(true, "pgsql"));
        assertEquals(false, arguments.getBooleanValueForDriver(false, "postgresql"));
    }

    @Test
    void storesDriverAwareBooleanData() {
        Arguments arguments = Arguments.builder()
                .addData("mysql_flag", true, "mysql")
                .addData("pgsql_flag", false, "pgsql");

        assertEquals(1, arguments.getData("mysql_flag"));
        assertEquals(false, arguments.getData("pgsql_flag"));
    }

    @Test
    void rejectsUnknownDriverForBooleanConversion() {
        Arguments arguments = Arguments.builder();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> arguments.getBooleanValueForDriver(true, "oracle")
        );

        assertTrue(exception.getMessage().contains("Unsupported driver"));
    }
}
