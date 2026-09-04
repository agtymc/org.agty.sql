package org.agty.sql.base;

import org.agty.sql.data.Arguments;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.support.SqlTextUtils;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Строка с данными
 */
public class RowData extends LinkedHashMap<String, Object> implements SqlRow {
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

    private boolean dataIsString = false;

    private Object getValue(String key) {
        if (key == null) {
            return null;
        }

        if (containsKey(key)) {
            return get(key);
        }

        String lowerCaseKey = key.toLowerCase(Locale.ROOT);
        if (containsKey(lowerCaseKey)) {
            return get(lowerCaseKey);
        }

        String upperCaseKey = key.toUpperCase(Locale.ROOT);
        if (containsKey(upperCaseKey)) {
            return get(upperCaseKey);
        }

        return null;
    }

    /**
     * Конвертирует и возвращает SqlRow из Arguments
     *
     * @param arguments Arguments
     * @return SqlRow
     */
    @Override
    public SqlRow convertFromArguments(Arguments arguments) {
        Map<String, Object> data = arguments.getDataMap();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            setData(entry.getKey(), entry.getValue());
        }

        return this;
    }

    /**
     * Все данные являются строкой
     *
     * @param isString true если все данные это строки
     * @return SqlRo
     */
    @Override
    public SqlRow setValuesAsString(Boolean isString) {
        this.dataIsString = isString;
        return this;
    }

    @Override
    public boolean isDataStringified() {
        return dataIsString;
    }

    /**
     * @deprecated use {@link #setValuesAsString(Boolean)}
     */
    @Override
    @Deprecated
    public SqlRow setDataIsString(Boolean isString) {
        return setValuesAsString(isString);
    }

    /**
     * @deprecated use {@link #isDataStringified()}
     */
    @Override
    @Deprecated
    public boolean dataIsString() {
        return isDataStringified();
    }

    /**
     * Добавить данные.
     *
     * @param key   имя столбца/данных.
     * @param value значение типа Object.
     */
    @Override
    public SqlRow setData(String key, Object value) {
        if (key != null) {
            put(key, value);
        }
        return this;
    }

    /**
     * Возвращает данные в виде объекта
     *
     * @param key имя столбца/данных.
     * @return значение типа Object.
     */
    @Override
    public Object getObject(String key) {
        return getValue(key);
    }

    /**
     * Возвращает строковое значение.
     *
     * @param key имя ключа.
     * @return данные.
     */
    @Override
    public String getString(String key) {
        Object value = getValue(key);
        if (value == null) return null;
        return isDataStringified() ? value.toString() : String.valueOf(value) ;
    }

    /**
     * Строковое значение с перекодировкой
     *
     * @param key имя ключа.
     * @return данные.
     */
    @Override
    public String getEstring(String key) {
        return SqlTextUtils.hencode(getString(key));
    }

    /**
     * Строковое значение с декодировкой
     *
     * @param key имя ключа.
     * @return данные.
     */
    @Override
    public String getDstring(String key) {
        return SqlTextUtils.hdecode(getString(key));
    }

    /**
     * Возвращает цифровое значение.
     *
     * @param key имя ключа.
     * @return данные.
     */
    @Override
    public Integer getInt(String key) {
        Object value = getValue(key);
        if (value == null) return null;
        if (value instanceof Number number) return number.intValue();
        if (isDataStringified()) return Integer.parseInt(value.toString());
        throw unsupportedNumericType(key, value);
    }

    /**
     * Возвращает цифровое значение Long.
     *
     * @param key имя ключа.
     * @return данные.
     */
    @Override
    public Long getLong(String key) {
        Object value = getValue(key);
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        if (isDataStringified()) return Long.parseLong(value.toString());
        throw unsupportedNumericType(key, value);
    }

    /**
     * Возвращает цифровое значение Double.
     *
     * @param key имя ключа.
     * @return данные.
     */
    @Override
    public Double getDouble(String key) {
        Object value = getValue(key);
        if (value == null) return null;
        if (value instanceof Number number) return number.doubleValue();
        if (isDataStringified()) return Double.parseDouble(value.toString());
        throw unsupportedNumericType(key, value);
    }

    /**
     * Возвращает цифровое значение Float.
     *
     * @param key имя ключа.
     * @return данные.
     */
    @Override
    public Float getFloat(String key) {
        Object value = getValue(key);
        if (value == null) return null;
        if (value instanceof Number number) return number.floatValue();
        if (isDataStringified()) return Float.parseFloat(value.toString());
        throw unsupportedNumericType(key, value);
    }

    /**
     * Возвращает цифровое значение Short.
     *
     * @param key имя ключа.
     * @return данные.
     */
    @Override
    public Short getShort(String key) {
        Object value = getValue(key);
        if (value == null) return null;
        if (value instanceof Number number) return number.shortValue();
        if (isDataStringified()) return Short.parseShort(value.toString());
        throw unsupportedNumericType(key, value);
    }

    private IllegalArgumentException unsupportedNumericType(String key, Object value) {
        return new IllegalArgumentException(
                "Unsupported numeric value for key '" + key + "': " + value.getClass().getName()
        );
    }

    /**
     * Возвращает символьное значение.
     *
     * @param key имя ключа.
     * @return данные.
     */
    @Override
    public Character getChar(String key) {
        Object value = getValue(key);
        if (value == null) return null;
        return (Character) value;
    }

    /**
     * Наличие объекта.
     *
     * @param key имя ключа.
     * @return true если данные есть.
     */
    @Override
    public boolean isSet(String key) {
        return getValue(key) != null;
    }

    /**
     * Проверяет не пустой ли массив данных
     *
     * @return bool
     */
    @Override
    public boolean isNotEmpty() {
        return !super.isEmpty();
    }

    /**
     * @deprecated use {@link #isNotEmpty()}
     */
    @Override
    @Deprecated
    public boolean noEmpty() {
        return isNotEmpty();
    }

    /**
     * Проверяет не пустой ли массив данных
     *
     * @return bool
     */
    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    /**
     * Возвращает true если значение == true, иначе false;
     * @param key имя ключа.
     * @return true если утверждение истинно.
     */
    @Override
    public Boolean getBoolean(String key) {
        Object value = getValue(key);

        if (value == null) {
            return null;
        }

        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        if (value instanceof Number numberValue) {
            return numberValue.doubleValue() != 0D;
        }

        if (value instanceof Character characterValue) {
            return parseBooleanValue(String.valueOf(characterValue), key);
        }

        if (value instanceof CharSequence charSequenceValue) {
            return parseBooleanValue(charSequenceValue.toString(), key);
        }

        return null;
    }

    private Boolean parseBooleanValue(String rawValue, String key) {
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

    /**
     * Возвращает дату
     *
     * @param key имя ключа.
     * @return дата
     */
    @Override
    public Date getDate(String key) {
        Object dateObject = getValue(key);

        if (dateObject == null) return null;

        if (dateObject instanceof Date date) {
            return new Date(date.getTime());
        }

        if (dateObject instanceof Instant instant) {
            return Date.from(instant);
        }

        if (dateObject instanceof LocalDateTime dateTime) {
            return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        }

        if (dateObject instanceof LocalDate date) {
            return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        if (dateObject instanceof OffsetDateTime offsetDateTime) {
            return Date.from(offsetDateTime.toInstant());
        }

        if (dateObject instanceof ZonedDateTime zonedDateTime) {
            return Date.from(zonedDateTime.toInstant());
        }

        if (dateObject instanceof CharSequence dateText) {
            return parseDateString(dateText.toString());
        }

        return null;
    }

    private Date parseDateString(String rawValue) {
        if (rawValue == null) {
            return null;
        }

        String normalizedValue = rawValue.trim();
        if (normalizedValue.isEmpty()) {
            return null;
        }

        try {
            return Date.from(Instant.parse(normalizedValue));
        } catch (DateTimeParseException ignored) {
        }

        try {
            return Date.from(OffsetDateTime.parse(normalizedValue, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant());
        } catch (DateTimeParseException ignored) {
        }

        try {
            return Date.from(ZonedDateTime.parse(normalizedValue, DateTimeFormatter.ISO_ZONED_DATE_TIME).toInstant());
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

    /**
     * Возвращает дату в объекте LocalDate
     *
     * @param key имя ключа.
     * @return дата
     */
    @Override
    public LocalDate getLocalDate(String key) {
        Object dateObject = getValue(key);

        if (dateObject instanceof LocalDate dateLocal) {
            return dateLocal;
        }

        if (dateObject instanceof LocalDateTime dateTime) {
            return dateTime.toLocalDate();
        }

        if (dateObject instanceof java.sql.Date date) {
            return date.toLocalDate();
        }

        if (dateObject instanceof Date date) {
            return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        }

        if (dateObject instanceof Instant instant) {
            return instant.atZone(ZoneId.systemDefault()).toLocalDate();
        }

        if (dateObject instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toLocalDate();
        }

        if (dateObject instanceof ZonedDateTime zonedDateTime) {
            return zonedDateTime.toLocalDate();
        }

        if (dateObject instanceof CharSequence) {
            Date parsed = getDate(key);
            return parsed == null
                    ? null
                    : parsed.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        return null;
    }

    /**
     * Возвращает дату в объекте LocalTime
     *
     * @param key имя ключа.
     * @return дата
     */
    @Override
    public Integer getYear(String key) {
        LocalDate date = getLocalDate(key);
        return date == null ? null : date.getYear();
    }

    /**
     * Возвращает дату в объекте LocalTime
     *
     * @param key имя ключа.
     * @return дата
     */
    @Override
    public LocalTime getLocalTime(String key) {
        Object dateObject = getValue(key);

        if (dateObject instanceof LocalTime localTime) {
            return localTime;
        }

        if (dateObject instanceof LocalDateTime dateTime) {
            return dateTime.toLocalTime();
        }

        if (dateObject instanceof java.sql.Time time) {
            return time.toLocalTime();
        }

        if (dateObject instanceof Date date) {
            return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalTime();
        }

        if (dateObject instanceof Instant instant) {
            return instant.atZone(ZoneId.systemDefault()).toLocalTime();
        }

        if (dateObject instanceof OffsetTime offsetTime) {
            return offsetTime.toLocalTime();
        }

        if (dateObject instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toLocalTime();
        }

        if (dateObject instanceof ZonedDateTime zonedDateTime) {
            return zonedDateTime.toLocalTime();
        }

        return null;
    }

    /**
     * Возвращает дату в объекте LocalDateTime
     *
     * @param key имя ключа.
     * @return дата
     */
    @Override
    public LocalDateTime getLocalDateTime(String key) {
        Object dateObject = getValue(key);

        if (dateObject instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }

        if (dateObject instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }

        if (dateObject instanceof LocalDate localDate) {
            return localDate.atStartOfDay();
        }

        if (dateObject instanceof Date date) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
        }

        if (dateObject instanceof Instant instant) {
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        }

        if (dateObject instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toLocalDateTime();
        }

        if (dateObject instanceof ZonedDateTime zonedDateTime) {
            return zonedDateTime.toLocalDateTime();
        }

        if (dateObject instanceof CharSequence) {
            Date parsed = getDate(key);
            return parsed == null
                    ? null
                    : LocalDateTime.ofInstant(parsed.toInstant(), ZoneId.systemDefault());
        }

        return null;
    }

    /**
     * Возвращает форматированную дату
     *
     * @param key имя ключа.
     * @return дата
     */
    @Override
    public String getDateFormat(String key, String format) {
        if (format == null || format.isEmpty()) format = "yyyy-MM-dd HH:mm:ss";

        Object dateObject = getValue(key);

        if (dateObject instanceof Date date) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            return simpleDateFormat.format(date);
        }

        if (dateObject instanceof LocalDate dateLocal) {
            return dateLocal.format(DateTimeFormatter.ofPattern(format));
        }

        if (dateObject instanceof LocalDateTime dateLocalTime) {
            return dateLocalTime.format(DateTimeFormatter.ofPattern(format));
        }

        if (dateObject instanceof LocalTime localTime) {
            return localTime.format(DateTimeFormatter.ofPattern(format));
        }

        if (dateObject instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.format(DateTimeFormatter.ofPattern(format));
        }

        if (dateObject instanceof ZonedDateTime zonedDateTime) {
            return zonedDateTime.format(DateTimeFormatter.ofPattern(format));
        }

        if (dateObject instanceof Instant instant) {
            return DateTimeFormatter.ofPattern(format)
                    .withZone(ZoneId.systemDefault())
                    .format(instant);
        }

        if (dateObject instanceof CharSequence) {
            Date parsed = getDate(key);
            if (parsed != null) {
                return new SimpleDateFormat(format).format(parsed);
            }
        }

        return null;
    }

    @Override
    public String toString() {
        StringBuilder toString = new StringBuilder();

        if (isEmpty()) return "RowData {}";

        toString.append("RowData");
        toString.append( " {\n");
        for(Map.Entry<String, Object> entry: this.entrySet()) {
            toString.append("\t");
            toString.append(entry.getKey());
            toString.append(" = ");
            toString.append(entry.getValue());
            toString.append("\n");
        }
        toString.append("}");

        return toString.toString();

    }
}
