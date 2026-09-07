package org.agty.sql.interfaces;

import org.agty.sql.base.Field;

import java.util.ArrayList;

/**
 * The field type interface
 */
public interface FieldsTypes {
    /**
     * Returns the fields.
     * @return operation result
     */
    ArrayList<Field> getFields();
    /**
     * Returns the field by agty sql type.
     * @param agtySqlType parameter value
     * @return operation result
     */
    Field getFieldByAgtySqlType(String agtySqlType);
    /**
     * Returns the field by driver sql type.
     * @param driverSqlType parameter value
     * @return operation result
     */
    Field getFieldByDriverSqlType(String driverSqlType);
}
