package org.agty.sql;

import org.agty.sql.base.RowData;
import org.agty.sql.base.RowDataEmpty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

class RowDataConversionTest {

    @Test
    void convertsEveryCommonNumberImplementation() {
        RowData row = new RowData();
        row.setData("byte", (byte) 12);
        row.setData("short", (short) 13);
        row.setData("integer", 14);
        row.setData("long", 15L);
        row.setData("bigInteger", BigInteger.valueOf(16));
        row.setData("float", 17.5F);
        row.setData("double", 18.5D);
        row.setData("decimal", new BigDecimal("19.5"));

        Assertions.assertEquals(12, row.getInt("byte"));
        Assertions.assertEquals(13L, row.getLong("short"));
        Assertions.assertEquals(14D, row.getDouble("integer"));
        Assertions.assertEquals(15F, row.getFloat("long"));
        Assertions.assertEquals((short) 16, row.getShort("bigInteger"));
        Assertions.assertEquals(17, row.getInt("float"));
        Assertions.assertEquals(18L, row.getLong("double"));
        Assertions.assertEquals(19.5D, row.getDouble("decimal"));
    }

    @Test
    void convertsStringifiedNumbersConsistently() {
        RowData row = new RowData();
        row.setValuesAsString(true);
        row.setData("integer", "12");
        row.setData("long", "13");
        row.setData("double", "14.5");
        row.setData("float", "15.5");
        row.setData("short", "16");

        Assertions.assertEquals(12, row.getInt("INTEGER"));
        Assertions.assertEquals(13L, row.getLong("LONG"));
        Assertions.assertEquals(14.5D, row.getDouble("DOUBLE"));
        Assertions.assertEquals(15.5F, row.getFloat("FLOAT"));
        Assertions.assertEquals((short) 16, row.getShort("SHORT"));
    }

    @Test
    void convertsDatesWithoutMonthOrTypeFailures() {
        RowData row = new RowData();
        Date january = new GregorianCalendar(2026, Calendar.JANUARY, 15, 12, 30, 45).getTime();
        row.setData("CREATED_AT", january);
        row.setData("date_only", LocalDate.of(2026, 2, 3));
        row.setData("time_only", LocalTime.of(10, 20, 30));
        row.setData("date_time", LocalDateTime.of(2026, 4, 5, 6, 7, 8));

        Assertions.assertEquals(LocalDate.of(2026, 1, 15), row.getLocalDate("created_at"));
        Assertions.assertNull(row.getLocalTime("date_only"));
        Assertions.assertEquals(LocalTime.of(10, 20, 30), row.getLocalTime("TIME_ONLY"));
        Assertions.assertEquals(LocalDate.of(2026, 4, 5), row.getLocalDate("DATE_TIME"));
        Assertions.assertEquals(2026, row.getYear("created_at"));
        Assertions.assertEquals("2026-04-05", row.getDateFormat("DATE_TIME", "yyyy-MM-dd"));

        LocalDate expectedDate = january.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Assertions.assertEquals(expectedDate, row.getLocalDate("CREATED_AT"));
    }

    @Test
    void emptyRowsUseNullForMissingValuesAndHaveValidStringRepresentation() {
        RowData row = new RowData();
        RowDataEmpty empty = new RowDataEmpty();

        Assertions.assertEquals("RowData {}", row.toString());
        Assertions.assertEquals("RowData {}", empty.toString());
        Assertions.assertNull(empty.getBoolean("missing"));
        Assertions.assertNull(empty.getDateFormat("missing", null));
        Assertions.assertSame(empty, empty.convertFromArguments(null));
    }
}
