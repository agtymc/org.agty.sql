package org.agty.sql;

import org.agty.sql.data.SqlQueryRebuild;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SqlQueryRebuildTest {

    @Test
    void rebuildsOnlyStructuralMarkers() {
        SqlQueryRebuild rebuild = rebuild(
                "SELECT [id], [display name] FROM {users} WHERE [id] = 1"
        );

        Assertions.assertEquals(
                "SELECT `id`, `display name` FROM `app_users` WHERE `id` = 1",
                rebuild.rebuildAndGet()
        );
        Assertions.assertTrue(rebuild.isQueryIsChanged());
    }

    @Test
    void preservesEmptyAndEscapedQuotedContent() {
        String query = "SELECT '', \"\", 'O''Reilly', \"a\"\"b\", 'backslash\\'quote', "
                + "'{users}', \"[column]\" FROM {users} WHERE [name] = '[literal]'";

        Assertions.assertEquals(
                "SELECT '', \"\", 'O''Reilly', \"a\"\"b\", 'backslash\\'quote', "
                        + "'{users}', \"[column]\" FROM `app_users` WHERE `name` = '[literal]'",
                rebuild(query).rebuildAndGet()
        );
    }

    @Test
    void preservesMarkersInCommentsAndPostgresDollarQuotedStrings() {
        String query = "SELECT [id], $$ {table} [column] $$, $body${other}[value]$body$ "
                + "FROM {users} -- {line} [line]\n"
                + "WHERE [id] = 1 /* {block} [block] */";

        Assertions.assertEquals(
                "SELECT `id`, $$ {table} [column] $$, $body${other}[value]$body$ "
                        + "FROM `app_users` -- {line} [line]\n"
                        + "WHERE `id` = 1 /* {block} [block] */",
                rebuild(query).rebuildAndGet()
        );
    }

    @Test
    void doesNotCorruptUnclosedQuotesOrPlaceholderLikeText() {
        String query = "SELECT '$(0) {users} [id]";
        SqlQueryRebuild rebuild = rebuild(query);

        Assertions.assertEquals(query, rebuild.rebuildAndGet());
        Assertions.assertFalse(rebuild.isQueryIsChanged());
    }

    @Test
    void leavesQueriesWithoutStructuralMarkersUnchanged() {
        SqlQueryRebuild rebuild = rebuild("SELECT 'plain text'");

        Assertions.assertEquals("SELECT 'plain text'", rebuild.rebuildAndGet());
        Assertions.assertFalse(rebuild.isQueryIsChanged());
    }

    private SqlQueryRebuild rebuild(String query) {
        return new SqlQueryRebuild(query)
                .setPrefix("app_")
                .setQuoteTable("`")
                .setQuoteColumn("`");
    }
}
