package org.agty.sql.model.entity;

/**
 * Provides column entity behavior.
 */
public class ColumnEntity {
    private boolean columnIsId;
    private boolean forceId;
    private String column;
    private Object value;
    private String type;
    private String defaultValue;

    /**
     * Creates a new instance.
     */
    public ColumnEntity() {}

    /**
     * Creates a new instance.
     * @param column parameter value
     * @param value parameter value
     */
    public ColumnEntity(String column, Object value) {
        this.column = column;
        this.value = value;
    }

    /**
     * Creates a new instance.
     * @param column parameter value
     * @param value parameter value
     * @param columnIsId parameter value
     */
    public ColumnEntity(String column, String value, boolean columnIsId) {
        this.column = column;
        this.value = value;
        this.columnIsId = columnIsId;
    }

    /**
     * Returns the column.
     * @return operation result
     */
    public String getColumn() {
        return column;
    }

    /**
     * Sets the column.
     * @param column parameter value
     */
    public void setColumn(String column) {
        this.column = column;
    }

    /**
     * Returns the value.
     * @return operation result
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the digit value.
     * @return operation result
     */
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

        if (value instanceof Number number) {
            return number.longValue();
        }

        throw new IllegalArgumentException("Column value is not numeric: " + value);
    }

    /**
     * Returns the string value.
     * @return operation result
     */
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

    /**
     * Performs the value is digit operation.
     * @return operation result
     */
    public boolean valueIsDigit() {
        return value instanceof Long
               || value instanceof Integer
               || value instanceof Byte
               || value instanceof Short;
    }

    /**
     * Performs the column is string operation.
     * @return operation result
     */
    public boolean columnIsString() {
        return getType() != null && getType().equals("String");
    }

    /**
     * Sets the value.
     * @param value parameter value
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Returns the type.
     * @return operation result
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     * @param type parameter value
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the default value.
     * @return operation result
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default value.
     * @param defaultValue parameter value
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Sets the column is id.
     * @param columnIsId parameter value
     */
    public void setColumnIsId(boolean columnIsId) {
        this.columnIsId = columnIsId;
    }

    /**
     * Performs the column is id operation.
     * @return operation result
     */
    public boolean columnIsId() {
        return columnIsId;
    }

    /**
     * Performs the column is exist operation.
     * @return operation result
     */
    public boolean columnIsExist() {
        return column != null;
    }

    /**
     * Returns whether force id.
     * @return operation result
     */
    public boolean isForceId() {
        return forceId;
    }

    /**
     * Sets the force id.
     * @param forceId parameter value
     */
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
