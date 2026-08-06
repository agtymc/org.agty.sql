package org.agty.sql;

import org.agty.sql.base.RowData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Date;

class RowDataDateParsingTest {

    @Test
    void supportsDateAndJavaTimeObjects() {
        RowData row = new RowData();
        Instant instant = Instant.parse("2026-08-06T10:15:30Z");
        LocalDateTime localDateTime = LocalDateTime.of(2026, 8, 6, 13, 45, 10);
        LocalDate localDate = LocalDate.of(2026, 8, 6);
        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2026-08-06T13:45:10+03:00");
        ZonedDateTime zonedDateTime = ZonedDateTime.parse("2026-08-06T13:45:10+03:00[Europe/Moscow]");
        Date utilDate = Date.from(instant);

        row.setData("utilDate", utilDate);
        row.setData("instant", instant);
        row.setData("localDateTime", localDateTime);
        row.setData("localDate", localDate);
        row.setData("offsetDateTime", offsetDateTime);
        row.setData("zonedDateTime", zonedDateTime);

        Assertions.assertEquals(instant, row.getDate("utilDate").toInstant());
        Assertions.assertEquals(instant, row.getDate("instant").toInstant());
        Assertions.assertEquals(localDateTime, toLocalDateTime(row.getDate("localDateTime")));
        Assertions.assertEquals(localDate, toLocalDateTime(row.getDate("localDate")).toLocalDate());
        Assertions.assertEquals(offsetDateTime.toInstant(), row.getDate("offsetDateTime").toInstant());
        Assertions.assertEquals(zonedDateTime.toInstant(), row.getDate("zonedDateTime").toInstant());
    }

    @Test
    void parsesPopularStringDateFormats() {
        RowData row = new RowData();
        row.setData("isoInstant", "2026-08-06T10:15:30Z");
        row.setData("isoOffset", "2026-08-06T13:15:30+03:00");
        row.setData("isoLocalDateTime", "2026-08-06T13:15:30");
        row.setData("sqlDateTime", "2026-08-06 13:15:30");
        row.setData("sqlDateTimeMillis", "2026-08-06 13:15:30.123");
        row.setData("slashDateTime", "2026/08/06 13:15:30");
        row.setData("ruDateTime", "06.08.2026 13:15:30");
        row.setData("dashDateTime", "06-08-2026 13:15:30");
        row.setData("usDateTime", "08/06/2026 13:15:30");
        row.setData("isoDate", "2026-08-06");
        row.setData("slashDate", "2026/08/06");
        row.setData("ruDate", "06.08.2026");
        row.setData("dashDate", "06-08-2026");
        row.setData("usDate", "08/06/2026");
        row.setData("basicIsoDate", "20260806");
        row.setData("compactDateTime", "20260806131530");

        Assertions.assertEquals(Instant.parse("2026-08-06T10:15:30Z"), row.getDate("isoInstant").toInstant());
        Assertions.assertEquals(Instant.parse("2026-08-06T10:15:30Z"), row.getDate("isoOffset").toInstant());
        Assertions.assertEquals(LocalDateTime.of(2026, 8, 6, 13, 15, 30), toLocalDateTime(row.getDate("isoLocalDateTime")));
        Assertions.assertEquals(LocalDateTime.of(2026, 8, 6, 13, 15, 30), toLocalDateTime(row.getDate("sqlDateTime")));
        Assertions.assertEquals(LocalDateTime.of(2026, 8, 6, 13, 15, 30, 123_000_000), toLocalDateTime(row.getDate("sqlDateTimeMillis")));
        Assertions.assertEquals(LocalDateTime.of(2026, 8, 6, 13, 15, 30), toLocalDateTime(row.getDate("slashDateTime")));
        Assertions.assertEquals(LocalDateTime.of(2026, 8, 6, 13, 15, 30), toLocalDateTime(row.getDate("ruDateTime")));
        Assertions.assertEquals(LocalDateTime.of(2026, 8, 6, 13, 15, 30), toLocalDateTime(row.getDate("dashDateTime")));
        Assertions.assertEquals(LocalDateTime.of(2026, 8, 6, 13, 15, 30), toLocalDateTime(row.getDate("usDateTime")));
        Assertions.assertEquals(LocalDate.of(2026, 8, 6), toLocalDateTime(row.getDate("isoDate")).toLocalDate());
        Assertions.assertEquals(LocalDate.of(2026, 8, 6), toLocalDateTime(row.getDate("slashDate")).toLocalDate());
        Assertions.assertEquals(LocalDate.of(2026, 8, 6), toLocalDateTime(row.getDate("ruDate")).toLocalDate());
        Assertions.assertEquals(LocalDate.of(2026, 8, 6), toLocalDateTime(row.getDate("dashDate")).toLocalDate());
        Assertions.assertEquals(LocalDate.of(2026, 8, 6), toLocalDateTime(row.getDate("usDate")).toLocalDate());
        Assertions.assertEquals(LocalDate.of(2026, 8, 6), toLocalDateTime(row.getDate("basicIsoDate")).toLocalDate());
        Assertions.assertEquals(LocalDateTime.of(2026, 8, 6, 13, 15, 30), toLocalDateTime(row.getDate("compactDateTime")));
    }

    @Test
    void returnsNullForInvalidOrBlankDates() {
        RowData row = new RowData();
        row.setData("blank", "   ");
        row.setData("invalid", "not-a-date");
        row.setData("unsupported", 12345);

        Assertions.assertNull(row.getDate("missing"));
        Assertions.assertNull(row.getDate("blank"));
        Assertions.assertNull(row.getDate("invalid"));
        Assertions.assertNull(row.getDate("unsupported"));
    }

    @Test
    void resolvesDateKeysCaseInsensitively() {
        RowData row = new RowData();
        row.setData("CREATED_AT", "2026-08-06 13:15:30");

        Assertions.assertEquals(
                LocalDateTime.of(2026, 8, 6, 13, 15, 30),
                toLocalDateTime(row.getDate("created_at"))
        );
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}
