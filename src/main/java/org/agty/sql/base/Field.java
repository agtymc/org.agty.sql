package org.agty.sql.base;

/**
 * Сопоставимое поле AgtySQL с полем в драйвере SQL
 * agtySqlType "blob" <-> driverSqlType(MySQL) "BYTEA"
 * agtySqlType "varchar" <-> driverSqlType(PgSQL) "CHARACTER VARYING"
 */
public class Field {
    /**
     * AgtySql type
     */
    private final String agtySqlType;

    /**
     * DriverSqlType (MySQL, PgSQL, etc)
     */
    private final String driverSqlType;

    /**
     * Field length
     * DECIMAL(fieldLength) -> DECIMAL(65,2)
     * VARCHAR(fieldLength) -> VARCHAR(120)
     */
    private final String fieldLength;

    /**
     * Constructor
     * @param agtySqlType AgtySql field type
     * @param driverSqlType DriverSql (MySQL, PgSQL, etc) field type
     * @param fieldLength Field length
     */
    public Field(String agtySqlType, String driverSqlType, String fieldLength) {
        this.agtySqlType = agtySqlType;
        this.driverSqlType = driverSqlType;
        this.fieldLength = fieldLength;
    }

    /**
     * Short constructor
     * @param agtySqlType AgtySql field type
     * @param driverSqlType DriverSql (MySQL, PgSQL, etc) field type
     */
    public Field(String agtySqlType, String driverSqlType) {
        this(agtySqlType, driverSqlType, null);
    }

    /**
     * Getter AgtySql Type
     * @return String type
     */
    public String getAgtySqlType() {
        return agtySqlType;
    }

    /**
     * Getter DriverSql Type
     * @return String type
     */
    public String getDriverSqlType() {
        return driverSqlType;
    }

    /**
     * Getter field length
     * @return String length
     */
    public String getFieldLength() {
        return fieldLength;
    }

    /**
     * Is field equals to AgtySQL format
     * @param type field type
     * @return bool
     */
    public boolean equalsAgtySql(String type) {
        return getAgtySqlType().equals(type);
    }

    /**
     * Is field equals to driver SQL format
     * @param type field type
     * @return bool
     */
    public boolean equalsDriverSql(String type) {
        return getDriverSqlType().equals(type);
    }
}
