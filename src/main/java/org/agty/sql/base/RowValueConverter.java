package org.agty.sql.base;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Stateless conversions used by the mutable {@link RowData} facade.
 */
final class RowValueConverter {
    private static final List<DateTimeFormatter> LOCAL_DATE_TIME_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss"),
            DateTimeFormatter.ofPattern("yyyyMMddHHmm")
    );

    private static final List<DateTimeFormatter> LOCAL_DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.BASIC_ISO_DATE
    );

    private RowValueConverter() {
    }

    static Integer asInteger(Object value, String key, boolean stringified) {
        if (value == null) return null;
        if (value instanceof Number number) return number.intValue();
        if (stringified) return Integer.parseInt(value.toString());
        throw unsupportedNumericType(key, value);
    }

    static Long asLong(Object value, String key, boolean stringified) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        if (stringified) return Long.parseLong(value.toString());
        throw unsupportedNumericType(key, value);
    }

    static Double asDouble(Object value, String key, boolean stringified) {
        if (value == null) return null;
        if (value instanceof Number number) return number.doubleValue();
        if (stringified) return Double.parseDouble(value.toString());
        throw unsupportedNumericType(key, value);
    }

    static Float asFloat(Object value, String key, boolean stringified) {
        if (value == null) return null;
        if (value instanceof Number number) return number.floatValue();
        if (stringified) return Float.parseFloat(value.toString());
        throw unsupportedNumericType(key, value);
    }

    static Short asShort(Object value, String key, boolean stringified) {
        if (value == null) return null;
        if (value instanceof Number number) return number.shortValue();
        if (stringified) return Short.parseShort(value.toString());
        throw unsupportedNumericType(key, value);
    }

    static Boolean asBoolean(Object value, String key) {
        if (value == null) return null;
        if (value instanceof Boolean booleanValue) return booleanValue;
        if (value instanceof Number numberValue) return numberValue.doubleValue() != 0D;
        if (value instanceof Character characterValue) {
            return parseBooleanValue(String.valueOf(characterValue), key);
        }
        if (value instanceof CharSequence charSequenceValue) {
            return parseBooleanValue(charSequenceValue.toString(), key);
        }
        return null;
    }

    static Date asDate(Object value) {
        if (value == null) return null;
        if (value instanceof Date date) return new Date(date.getTime());
        if (value instanceof Instant instant) return Date.from(instant);
        if (value instanceof LocalDateTime dateTime) {
            return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        }
        if (value instanceof LocalDate date) {
            return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        if (value instanceof OffsetDateTime offsetDateTime) return Date.from(offsetDateTime.toInstant());
        if (value instanceof ZonedDateTime zonedDateTime) return Date.from(zonedDateTime.toInstant());
        if (value instanceof CharSequence dateText) return parseDateString(dateText.toString());
        return null;
    }

    static LocalDate asLocalDate(Object value) {
        if (value instanceof LocalDate dateLocal) return dateLocal;
        if (value instanceof LocalDateTime dateTime) return dateTime.toLocalDate();
        if (value instanceof java.sql.Date date) return date.toLocalDate();
        if (value instanceof Date date) {
            return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (value instanceof Instant instant) return instant.atZone(ZoneId.systemDefault()).toLocalDate();
        if (value instanceof OffsetDateTime offsetDateTime) return offsetDateTime.toLocalDate();
        if (value instanceof ZonedDateTime zonedDateTime) return zonedDateTime.toLocalDate();
        if (value instanceof CharSequence) {
            Date parsed = asDate(value);
            return parsed == null ? null : parsed.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return null;
    }

    static LocalTime asLocalTime(Object value) {
        if (value instanceof LocalTime localTime) return localTime;
        if (value instanceof LocalDateTime dateTime) return dateTime.toLocalTime();
        if (value instanceof java.sql.Time time) return time.toLocalTime();
        if (value instanceof Date date) {
            return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalTime();
        }
        if (value instanceof Instant instant) return instant.atZone(ZoneId.systemDefault()).toLocalTime();
        if (value instanceof OffsetTime offsetTime) return offsetTime.toLocalTime();
        if (value instanceof OffsetDateTime offsetDateTime) return offsetDateTime.toLocalTime();
        if (value instanceof ZonedDateTime zonedDateTime) return zonedDateTime.toLocalTime();
        return null;
    }

    static LocalDateTime asLocalDateTime(Object value) {
        if (value instanceof LocalDateTime localDateTime) return localDateTime;
        if (value instanceof java.sql.Timestamp timestamp) return timestamp.toLocalDateTime();
        if (value instanceof LocalDate localDate) return localDate.atStartOfDay();
        if (value instanceof Date date) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
        }
        if (value instanceof Instant instant) return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        if (value instanceof OffsetDateTime offsetDateTime) return offsetDateTime.toLocalDateTime();
        if (value instanceof ZonedDateTime zonedDateTime) return zonedDateTime.toLocalDateTime();
        if (value instanceof CharSequence) {
            Date parsed = asDate(value);
            return parsed == null
                    ? null
                    : LocalDateTime.ofInstant(parsed.toInstant(), ZoneId.systemDefault());
        }
        return null;
    }

    static String formatDate(Object value, String format) {
        String pattern = format == null || format.isEmpty() ? "yyyy-MM-dd HH:mm:ss" : format;
        if (value instanceof Date date) return new SimpleDateFormat(pattern).format(date);
        if (value instanceof LocalDate dateLocal) {
            return dateLocal.format(DateTimeFormatter.ofPattern(pattern));
        }
        if (value instanceof LocalDateTime dateLocalTime) {
            return dateLocalTime.format(DateTimeFormatter.ofPattern(pattern));
        }
        if (value instanceof LocalTime localTime) {
            return localTime.format(DateTimeFormatter.ofPattern(pattern));
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.format(DateTimeFormatter.ofPattern(pattern));
        }
        if (value instanceof ZonedDateTime zonedDateTime) {
            return zonedDateTime.format(DateTimeFormatter.ofPattern(pattern));
        }
        if (value instanceof Instant instant) {
            return DateTimeFormatter.ofPattern(pattern)
                    .withZone(ZoneId.systemDefault())
                    .format(instant);
        }
        if (value instanceof CharSequence) {
            Date parsed = asDate(value);
            return parsed == null ? null : new SimpleDateFormat(pattern).format(parsed);
        }
        return null;
    }

    private static Boolean parseBooleanValue(String rawValue, String key) {
        String normalizedValue = rawValue == null ? null : rawValue.trim().toLowerCase(Locale.ROOT);
        if (normalizedValue == null || normalizedValue.isEmpty()) {
            throw new IllegalArgumentException("Unsupported boolean value for key '" + key + "': " + rawValue);
        }
        return switch (normalizedValue) {
            case "true", "t", "y", "yes", "on", "1" -> true;
            case "false", "f", "n", "no", "off", "0" -> false;
            default -> throw new IllegalArgumentException(
                    "Unsupported boolean value for key '" + key + "': " + rawValue
            );
        };
    }

    private static Date parseDateString(String rawValue) {
        if (rawValue == null) return null;
        String normalizedValue = rawValue.trim();
        if (normalizedValue.isEmpty()) return null;

        try {
            return Date.from(Instant.parse(normalizedValue));
        } catch (DateTimeParseException ignored) {
        }
        try {
            return Date.from(OffsetDateTime.parse(
                    normalizedValue,
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
            ).toInstant());
        } catch (DateTimeParseException ignored) {
        }
        try {
            return Date.from(ZonedDateTime.parse(
                    normalizedValue,
                    DateTimeFormatter.ISO_ZONED_DATE_TIME
            ).toInstant());
        } catch (DateTimeParseException ignored) {
        }
        for (DateTimeFormatter formatter : LOCAL_DATE_TIME_FORMATTERS) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(normalizedValue, formatter);
                return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
            } catch (DateTimeParseException ignored) {
            }
        }
        for (DateTimeFormatter formatter : LOCAL_DATE_FORMATTERS) {
            try {
                LocalDate date = LocalDate.parse(normalizedValue, formatter);
                return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private static IllegalArgumentException unsupportedNumericType(String key, Object value) {
        return new IllegalArgumentException(
                "Unsupported numeric value for key '" + key + "': " + value.getClass().getName()
        );
    }
}
