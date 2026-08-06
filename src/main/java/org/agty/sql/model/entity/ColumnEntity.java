package org.agty.sql.model.entity;

public class ColumnEntity {
    private boolean columnIsId;
    private boolean forceId;
    private String column;
    private Object value;
    private String type;
    private String defaultValue;

    public ColumnEntity() {}

    public ColumnEntity(String column, Object value) {
        setColumn(column);
        setValue(value);
    }

    public ColumnEntity(String column, String value, boolean columnIsId) {
        setColumn(column);
        setValue(value);
        setColumnIsId(columnIsId);
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public Object getValue() {
        return value;
    }

    public Long getDigitValue() {
        if (value instanceof Long longValue) {
            return longValue;
        }

        if (value instanceof Integer intValue) {
            return intValue.longValue();
        }

        if (value instanceof Byte byteValue) {
            return byteValue.longValue();
        }

        if (value instanceof Short shortValue) {
            return shortValue.longValue();
        }

        return (long) value;
    }

    public String getStringValue() {
        if (value instanceof String stringValue) {
            return stringValue;
        }

        if (value instanceof Integer intValue) {
            return intValue.toString();
        }

        if (value instanceof Long longValue) {
            return longValue.toString();
        }

        if (value instanceof Float floatValue) {
            return floatValue.toString();
        }

        if (value instanceof Double doubleValue) {
            return doubleValue.toString();
        }

        if (value instanceof Boolean booleanValue) {
            return booleanValue ? "1" : "0";
        }

        if (value instanceof Short shortValue) {
            return shortValue.toString();
        }

        return value != null ? value.toString() : null;
    }

    public boolean valueIsDigit() {
        return value instanceof Long
               || value instanceof Integer
               || value instanceof Byte
               || value instanceof Short;
    }

    public boolean columnIsString() {
        return getType() != null && getType().equals("String");
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setColumnIsId(boolean columnIsId) {
        this.columnIsId = columnIsId;
    }

    public boolean columnIsId() {
        return columnIsId;
    }

    public boolean columnIsExist() {
        return column != null;
    }

    public boolean isForceId() {
        return forceId;
    }

    public void setForceId(boolean forceId) {
        this.forceId = forceId;
    }

    @Override
    public String toString() {
        return "ColumnEntity{" +
                "columnIsId=" + columnIsId +
                ", column='" + column + '\'' +
                ", value='" + value + '\'' +
                ", type='" + type + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                '}';
    }
}
