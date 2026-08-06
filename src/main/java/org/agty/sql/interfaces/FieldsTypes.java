package org.agty.sql.interfaces;

import org.agty.sql.base.Field;

import java.util.ArrayList;

/**
 * The field type interface
 */
public interface FieldsTypes {
    ArrayList<Field> getFields();
    Field getFieldByAgtySqlType(String agtySqlType);
    Field getFieldByDriverSqlType(String driverSqlType);
}
