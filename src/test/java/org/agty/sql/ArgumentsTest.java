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
                .setData("name", "alice")
                .setData("age", 42)
                .addColumn("id")
                .addColumn("name");

        assertEquals("{users}", arguments.getTable());
        assertEquals("id", arguments.getActionField());
        assertEquals("alice", arguments.getFromData("name"));
        assertEquals(42, arguments.getFromData("age"));
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
        assertTrue(arguments.forceRequery());
        assertTrue(arguments.returnLastInsertId());
    }
}
