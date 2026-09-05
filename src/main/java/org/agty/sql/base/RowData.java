package org.agty.sql.base;

import org.agty.sql.data.Arguments;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.support.SqlTextUtils;

import java.time.*;
import java.util.*;

/**
 * Строка с данными
 */
public class RowData extends LinkedHashMap<String, Object> implements SqlRow {
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
        return RowValueConverter.asInteger(getValue(key), key, isDataStringified());
    }

    /**
     * Возвращает цифровое значение Long.
     *
     * @param key имя ключа.
     * @return данные.
     */
    @Override
    public Long getLong(String key) {
        return RowValueConverter.asLong(getValue(key), key, isDataStringified());
    }

    /**
     * Возвращает цифровое значение Double.
     *
     * @param key имя ключа.
     * @return данные.
     */
    @Override
    public Double getDouble(String key) {
        return RowValueConverter.asDouble(getValue(key), key, isDataStringified());
    }

    /**
     * Возвращает цифровое значение Float.
     *
     * @param key имя ключа.
     * @return данные.
     */
    @Override
    public Float getFloat(String key) {
        return RowValueConverter.asFloat(getValue(key), key, isDataStringified());
    }

    /**
     * Возвращает цифровое значение Short.
     *
     * @param key имя ключа.
     * @return данные.
     */
    @Override
    public Short getShort(String key) {
        return RowValueConverter.asShort(getValue(key), key, isDataStringified());
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
        return RowValueConverter.asBoolean(getValue(key), key);
    }

    /**
     * Возвращает дату
     *
     * @param key имя ключа.
     * @return дата
     */
    @Override
    public Date getDate(String key) {
        return RowValueConverter.asDate(getValue(key));
    }

    /**
     * Возвращает дату в объекте LocalDate
     *
     * @param key имя ключа.
     * @return дата
     */
    @Override
    public LocalDate getLocalDate(String key) {
        return RowValueConverter.asLocalDate(getValue(key));
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
        return RowValueConverter.asLocalTime(getValue(key));
    }

    /**
     * Возвращает дату в объекте LocalDateTime
     *
     * @param key имя ключа.
     * @return дата
     */
    @Override
    public LocalDateTime getLocalDateTime(String key) {
        return RowValueConverter.asLocalDateTime(getValue(key));
    }

    /**
     * Возвращает форматированную дату
     *
     * @param key имя ключа.
     * @return дата
     */
    @Override
    public String getDateFormat(String key, String format) {
        return RowValueConverter.formatDate(getValue(key), format);
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
