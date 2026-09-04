package org.agty.sql.base;

import org.agty.sql.interfaces.FieldsTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Массив сопоставлений типов полей AgtySQL и полей драйвера (MySQL, PgSQL, etc)
 */
public class FieldsType implements FieldsTypes {
    /**
     * Массив сопоставлений
     */
    private final List<Field> fields = new ArrayList<>();

    /**
     * Добавить поле в массив
     * @param field Field
     */
    protected final void add(Field field) {
        fields.add(field);
    }

    /**
     * Получить все поля
     * @return Fields array
     */
    public ArrayList<Field> getFields() {
        return new ArrayList<Field>(fields);
    }

    /**
     * Получить по типу поля AgtySQL
     * @param agtySqlType Field type
     * @return Field
     */
    public Field getFieldByAgtySqlType(String agtySqlType) {

        for (Field field : fields) {
            if (field.equalsAgtySql(agtySqlType)) {
                return field;
            }
        }

        return null;
    }

    /**
     * Получить по типу поля DriverSQL
     * @param driverSqlType Field type
     * @return Field
     */
    public Field getFieldByDriverSqlType(String driverSqlType) {

        for (Field field : fields) {
            if (field.equalsDriverSql(driverSqlType)) {
                return field;
            }
        }

        return null;
    }
}
