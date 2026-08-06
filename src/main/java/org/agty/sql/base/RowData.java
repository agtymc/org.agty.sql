package org.agty.sql.base;

import org.agty.sql.data.Arguments;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.support.SqlTextUtils;

import java.text.SimpleDateFormat;
import java.math.BigInteger;
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
        Map<String, Object> data = arguments.getDataArray();

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
    public SqlRow setDataIsString(Boolean isString) {
        this.dataIsString = isString;
        return this;
    }

    @Override
    public boolean dataIsString() {
        return dataIsString;
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
        return dataIsString() ? value.toString() : String.valueOf(value) ;
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

        if (value instanceof Long) {
            return ((Long) value).intValue();
        }

        if (value instanceof Double) {
            return ((Double) value).intValue();
        }

        if (value instanceof Float) {
            return ((Float) value).intValue();
        }

        if (value instanceof Short) {
            return ((Short) value).intValue();
        }

        if (value instanceof BigInteger bigIntegerValue) {
            return bigIntegerValue.intValue();
        }

        return dataIsString() ? Integer.parseInt(value.toString()) : (Integer) value;
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
        
        if (value instanceof Integer integerValue) {
            return Long.valueOf(integerValue);
        }

        if (value instanceof BigInteger bigIntegerValue) {
            return bigIntegerValue.longValue();
        }

        return dataIsString() ? Long.parseLong(value.toString()) : (Long) value;
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
        return dataIsString() ? Double.parseDouble(value.toString()) : (Double) value;
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
        return dataIsString() ? Float.parseFloat(value.toString()) : (Float) value;
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
        return dataIsString() ? Short.parseShort(value.toString()) : (Short) value;
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
    public boolean noEmpty() {
        return !super.isEmpty();
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
            return Date.from(date.toInstant());
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
        Object dateObject = get(key);

        if (dateObject instanceof LocalDate dateLocal) {
            return dateLocal;
        }

        if (dateObject instanceof LocalDateTime dateTime) {
            return LocalDate.from(dateTime);
        }

        if (dateObject instanceof Date date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
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
        Object dateObject = get(key);

        if (dateObject instanceof Date date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar.get(Calendar.YEAR);
        }

        if (dateObject instanceof LocalDate dateLocal) {
            return dateLocal.getYear();
        }

        if (dateObject instanceof LocalDateTime dateTime) {
            return dateTime.getYear();
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
    public LocalTime getLocalTime(String key) {
        Object dateObject = get(key);

        if (dateObject instanceof LocalDate dateLocal) {
            return LocalTime.from(dateLocal);
        }

        if (dateObject instanceof LocalDateTime dateTime) {
            return LocalTime.from(dateTime);
        }

        if (get(key) instanceof java.sql.Time time) {
            return time.toLocalTime();
        }

        if (dateObject instanceof Date date) {
            Instant instant = date.toInstant();
            ZoneId zoneId = TimeZone.getDefault().toZoneId();
            return LocalTime.ofInstant(instant, zoneId);
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
        Object dateObject = get(key);

        if (dateObject instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }

        if (dateObject instanceof Date date) {
            Instant instant = date.toInstant();
            ZoneId zoneId = TimeZone.getDefault().toZoneId();
            return LocalDateTime.ofInstant(instant, zoneId);
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

        Object dateObject = get(key);

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

        return null;
    }

    @Override
    public String toString() {
        StringBuilder toString = new StringBuilder();

        if (isEmpty()) return null;

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
