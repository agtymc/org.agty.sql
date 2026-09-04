package org.agty.sql.base;

import org.agty.sql.data.Arguments;
import org.agty.sql.interfaces.SqlRow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * Empty data row
 */
public class RowDataEmpty implements SqlRow {

    /**
     * Конвертирует и возвращает SqlRow из Arguments
     *
     * @param arguments Arguments
     * @return SqlRow
     */
    @Override
    public SqlRow convertFromArguments(Arguments arguments) {
        return this;
    }

    @Override
    public SqlRow setValuesAsString(Boolean isString) {
        return this;
    }

    @Override
    public boolean isDataStringified() {
        return false;
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

    @Override
    public SqlRow setData(String key, Object value) {
        return this;
    }

    @Override
    public Object getObject(String key) {
        return null;
    }

    @Override
    public String getString(String key) {
        return null;
    }

    @Override
    public String getEstring(String key) {
        return null;
    }

    @Override
    public String getDstring(String key) {
        return null;
    }

    @Override
    public Integer getInt(String key) {
        return null;
    }

    @Override
    public Long getLong(String key) {
        return null;
    }

    @Override
    public Double getDouble(String key) {
        return null;
    }

    @Override
    public Float getFloat(String key) {
        return null;
    }

    @Override
    public Short getShort(String key) {
        return null;
    }

    @Override
    public Character getChar(String key) {
        return null;
    }

    @Override
    public boolean isSet(String key) {
        return false;
    }

    @Override
    public boolean isNotEmpty() {
        return false;
    }

    /**
     * @deprecated use {@link #isNotEmpty()}
     */
    @Override
    @Deprecated
    public boolean noEmpty() {
        return isNotEmpty();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Boolean getBoolean(String key) {
        return null;
    }

    @Override
    public Date getDate(String key) {
        return null;
    }

    @Override
    public LocalDate getLocalDate(String key) {
        return null;
    }

    @Override
    public LocalDateTime getLocalDateTime(String key) {
        return null;
    }

    @Override
    public LocalTime getLocalTime(String key) {
        return null;
    }

    @Override
    public Integer getYear(String key) {
        return null;
    }

    @Override
    public String getDateFormat(String key, String format) {
        return null;
    }

    @Override
    public String toString() {
        return "RowData {}";
    }
}
