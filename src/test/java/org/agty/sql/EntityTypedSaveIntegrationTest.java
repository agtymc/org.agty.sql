package org.agty.sql;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.agty.sql.data.Arguments;
import org.agty.sql.model.annotations.Entity;
import org.agty.sql.model.annotations.Id;
import org.agty.sql.model.annotations.Table;
import org.agty.sql.support.TestDatabaseProfile;
import org.agty.sql.support.TestDatabaseProfiles;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class EntityTypedSaveIntegrationTest {
    static Stream<TestDatabaseProfile> profiles() { return TestDatabaseProfiles.sqlProfiles(); }

    @Entity
    @Table(name = "typed_entity_regression")
    public static class Sample {
        @Id public Long id;
        public LocalDateTime updated_at;
        public Boolean active;
        public BigDecimal amount;
        public java.time.LocalDate calendar_date;
        public java.time.LocalTime clock;
        public Double fraction;
        public String optional = "initializer";
        public Sample() {}
    }

    @ParameterizedTest(name = "typed entity INSERT/UPDATE: {0}")
    @MethodSource("profiles")
    void roundTrip(TestDatabaseProfile profile) {
        try (AgtySQL sql = profile.createSql()) {
            String table = "typed_entity_regression";
            sql.execute("DROP TABLE IF EXISTS " + table);
            String temporal = switch (profile.server()) {
                case "mssql" -> "DATETIME2(6)";
                case "mysql", "mariadb" -> "DATETIME(6)";
                case "sqlite" -> "TEXT";
                default -> "TIMESTAMP(6)";
            };
            String ddl = profile.createAutoIdTableDdl().replace("{table}", table)
                    .replace("string VARCHAR(255)", "updated_at " + temporal + ", active "
                            + (profile.server().equals("mssql") ? "BIT" : "BOOLEAN")
                            + ", amount DECIMAL(18,6), calendar_date DATE, clock TIME, fraction FLOAT, optional VARCHAR(255)");
            sql.execute(ddl);
            try {
                Sample input = new Sample();
                input.updated_at = LocalDateTime.of(2026, 9, 9, 12, 34, 56, 123456000);
                input.active = true;
                input.amount = new BigDecimal("12345.678901");
                input.optional = null;
                input.calendar_date = java.time.LocalDate.of(2026, 9, 9);
                input.clock = java.time.LocalTime.of(12, 34, 56);
                input.fraction = 1.25;
                Sample inserted = sql.saveEntityWithCheck(input);
                check(input, inserted);
                assertNotNull(inserted.id);
                assertEquals(1L, sql.countRows(Arguments.builder().useStatementPrepare(true).setTable(table)
                        .setWhere("[updated_at] = ?", input.updated_at)));
                assertEquals(1L, sql.countRows(Arguments.builder().useStatementPrepare(true).setTable(table)
                        .setWhere("[updated_at] > ? AND [updated_at] < ?", input.updated_at.minusNanos(1000), input.updated_at.plusNanos(1000))));
                if (profile.server().equals("sqlite")) {
                    assertEquals("2026-09-09 12:34:56.123456000", sql.fetch("SELECT updated_at FROM " + table).getString("updated_at"));
                    assertEquals("text", sql.fetch("SELECT typeof(updated_at) AS kind FROM " + table).getString("kind"));
                }
                inserted.updated_at = input.updated_at.plusDays(1);
                inserted.active = false;
                inserted.amount = new BigDecimal("-9.123456");
                inserted.optional = "saved";
                inserted.calendar_date = input.calendar_date.plusDays(1);
                inserted.clock = input.clock.plusSeconds(1);
                inserted.fraction = -2.5;
                Sample updated = sql.saveEntityWithCheck(inserted);
                check(inserted, updated);
                Sample fetched = sql.fetch(Arguments.builder().useStatementPrepare(true).setTable(table).setWhere("[id] = ?", updated.id), Sample.class);
                check(updated, fetched);
                updated.updated_at = null;
                updated.active = null;
                updated.amount = null;
                updated.optional = null;
                updated.calendar_date = null;
                updated.clock = null;
                updated.fraction = null;
                check(updated, sql.updateEntity(updated));
                Sample empty = new Sample();
                empty.optional = null;
                check(empty, sql.insertEntity(empty));
            } finally {
                sql.execute("DROP TABLE IF EXISTS " + table);
            }
        }
    }

    private static void check(Sample expected, Sample actual) {
        assertNotNull(actual);
        assertEquals(expected.updated_at, actual.updated_at);
        assertEquals(expected.active, actual.active);
        assertEquals(expected.calendar_date, actual.calendar_date);
        assertEquals(expected.clock, actual.clock);
        assertEquals(expected.fraction, actual.fraction);
        if (expected.amount == null) assertNull(actual.amount);
        else assertEquals(0, expected.amount.compareTo(actual.amount));
        assertEquals(expected.optional, actual.optional);
    }
}
