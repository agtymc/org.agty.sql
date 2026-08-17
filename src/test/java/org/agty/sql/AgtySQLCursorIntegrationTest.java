package org.agty.sql;

import org.agty.sql.data.Arguments;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.support.TestDatabaseProfile;
import org.agty.sql.support.TestDatabaseProfiles;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class AgtySQLCursorIntegrationTest {

    private static Stream<TestDatabaseProfile> sqlProfiles() {
        return TestDatabaseProfiles.sqlProfiles();
    }

    @ParameterizedTest(name = "cursor lifecycle: {0}")
    @MethodSource("sqlProfiles")
    @SuppressWarnings("deprecation")
    void managesListCursorLifecycle(TestDatabaseProfile profile) {
        AgtySQL sql = profile.createSql();
        String table = "{integration_cursor_" + profile.server() + "}";
        Arguments arguments = Arguments.builder()
                .setTable(table)
                .setOrderBy("id ASC");

        try {
            recreateTable(sql, profile, table);
            insertRows(sql, table);

            Assertions.assertFalse(sql.hasOpenListCursor(3));

            SqlRow first = sql.list(arguments, 3);
            Assertions.assertNotNull(first);
            Assertions.assertEquals(1L, first.getLong("id"));
            Assertions.assertTrue(sql.hasOpenListCursor(3));

            SqlRow second = sql.list(arguments, 3);
            Assertions.assertNotNull(second);
            Assertions.assertEquals(2L, second.getLong("id"));

            sql.closeListCursor(3);
            Assertions.assertFalse(sql.hasOpenListCursor(3));

            SqlRow reopened = sql.list(arguments, 3);
            Assertions.assertNotNull(reopened);
            Assertions.assertEquals(1L, reopened.getLong("id"));

            Assertions.assertNotNull(sql.list(arguments, 3));
            Assertions.assertNotNull(sql.list(arguments, 3));
            Assertions.assertNotNull(sql.list(arguments, 3));
            Assertions.assertNull(sql.list(arguments, 3));
            Assertions.assertFalse(sql.hasOpenListCursor(3));
        } finally {
            sql.closeListCursors();
            dropTableQuietly(sql, table);
            sql.close();
        }
    }

    @ParameterizedTest(name = "public cursor api: {0}")
    @MethodSource("sqlProfiles")
    void streamsRowsThroughPublicCursor(TestDatabaseProfile profile) {
        AgtySQL sql = profile.createSql();
        String table = "{integration_public_cursor_" + profile.server() + "}";

        try {
            recreateTable(sql, profile, table);
            insertRows(sql, table);

            try (AgtySqlCursor cursor = sql.openCursor(
                    Arguments.builder()
                            .setTable(table)
                            .setOrderBy("id ASC")
            )) {
                Assertions.assertNotNull(cursor);
                Assertions.assertFalse(cursor.isClosed());

                SqlRow first = cursor.next();
                SqlRow second = cursor.next();
                SqlRow third = cursor.next();
                SqlRow fourth = cursor.next();
                SqlRow end = cursor.next();

                Assertions.assertEquals(1L, first.getLong("id"));
                Assertions.assertEquals(2L, second.getLong("id"));
                Assertions.assertEquals(3L, third.getLong("id"));
                Assertions.assertEquals(4L, fourth.getLong("id"));
                Assertions.assertNull(end);
                Assertions.assertTrue(cursor.isClosed());
            }
        } finally {
            dropTableQuietly(sql, table);
            sql.close();
        }
    }

    @ParameterizedTest(name = "public cursor hasNext api: {0}")
    @MethodSource("sqlProfiles")
    void streamsRowsThroughPublicCursorHasNext(TestDatabaseProfile profile) {
        AgtySQL sql = profile.createSql();
        String table = "{integration_public_cursor_hasnext_" + profile.server() + "}";

        try {
            recreateTable(sql, profile, table);
            insertRows(sql, table);

            try (AgtySqlCursor cursor = sql.openCursor(
                    Arguments.builder()
                            .setTable(table)
                            .setOrderBy("id ASC")
            )) {
                List<Long> ids = new ArrayList<>();

                Assertions.assertTrue(cursor.hasNext());
                Assertions.assertTrue(cursor.hasNext());

                while (cursor.hasNext()) {
                    ids.add(cursor.next().getLong("id"));
                }

                Assertions.assertEquals(List.of(1L, 2L, 3L, 4L), ids);
                Assertions.assertFalse(cursor.hasNext());
                Assertions.assertNull(cursor.next());
                Assertions.assertTrue(cursor.isClosed());
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

    private static void insertRows(AgtySQL sql, String table) {
        for (int i = 1; i <= 4; i++) {
            sql.insert(
                    Arguments.builder()
                            .setTable(table)
                            .addData("id", i)
                            .addData("string", "value-" + i)
                            .addData("integers", i * 10)
                            .addData("bool", i % 2 == 0)
            );
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
