package org.agty.sql.model.builders;

import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.model.annotations.Column;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * Provides object builder behavior.
 */
public class ObjectBuilder {
    /** Creates a new ObjectBuilder instance. */
    public ObjectBuilder() {
    }

    private Object object;
    private Class<?> clazz;
    private SqlRow sqlRow;

    /**
     * Performs the builder operation.
     * @return operation result
     */
    public static ObjectBuilder builder() {
        return new ObjectBuilder();
    }

    /**
     * Performs the object operation.
     * @param object parameter value
     * @return operation result
     */
    public ObjectBuilder object(Object object) {
        this.object = object;
        return this;
    }

    /**
     * Performs the clazz operation.
     * @param clazz parameter value
     * @return operation result
     */
    public ObjectBuilder clazz(Class<?> clazz) {
        this.clazz = clazz;
        return this;
    }

    /**
     * Performs the sql row operation.
     * @param sqlRow parameter value
     * @return operation result
     */
    public ObjectBuilder sqlRow(SqlRow sqlRow) {
        this.sqlRow = sqlRow;
        return this;
    }

    /**
     * Performs the build operation.
     * @return operation result
     */
    public Object build() {
        Object newObjectInstance;

        try {
            if (clazz != null) {
                newObjectInstance = clazz.getDeclaredConstructor().newInstance();
            } else {
                newObjectInstance = object.getClass().getDeclaredConstructor().newInstance();
            }

            if (sqlRow.isNotEmpty()) {
                for (Field field : newObjectInstance.getClass().getDeclaredFields()) {
                    field.setAccessible(true);

                    String fieldName;
                    if (field.isAnnotationPresent(Column.class) && !field.getAnnotation(Column.class).name().isBlank()) {
                        fieldName = field.getAnnotation(Column.class).name();
                    } else {
                        fieldName = field.getName();
                    }

                    if (sqlRow.isSet(fieldName)) {

                        if (isFieldInstanceOfType(field, Integer.class)) {
                            field.set(newObjectInstance, sqlRow.getInt(fieldName));
                        }

                        else if (isFieldInstanceOfType(field, Long.class)) {
                            field.set(newObjectInstance, sqlRow.getLong(fieldName));
                        }

                        else if (isFieldInstanceOfType(field, Short.class)) {
                            field.set(newObjectInstance, sqlRow.getShort(fieldName));
                        }

                        else if (isFieldInstanceOfType(field, Double.class)) {
                            field.set(newObjectInstance, sqlRow.getDouble(fieldName));
                        }

                        else if (isFieldInstanceOfType(field, String.class)) {
                            field.set(newObjectInstance, sqlRow.getString(fieldName));
                        }

                        else if (isFieldInstanceOfType(field, Boolean.class)) {
                            field.set(newObjectInstance, sqlRow.getBoolean(fieldName));
                        }

                        else if (isFieldInstanceOfType(field, Character.class)) {
                            field.set(newObjectInstance, sqlRow.getChar(fieldName));
                        }

                        else if (isFieldInstanceOfType(field, Float.class)) {
                            field.set(newObjectInstance, sqlRow.getFloat(fieldName));
                        }

                        else if (isFieldInstanceOfType(field, LocalDateTime.class)) {
                            field.set(newObjectInstance, sqlRow.getLocalDateTime(fieldName));
                        }

                        else if (isFieldInstanceOfType(field, LocalDate.class)) {
                            field.set(newObjectInstance, sqlRow.getLocalDate(fieldName));
                        }

                        else if (isFieldInstanceOfType(field, LocalTime.class)) {
                            field.set(newObjectInstance, sqlRow.getLocalTime(fieldName));
                        }

                        else if (isFieldInstanceOfType(field, Date.class)) {
                            field.set(newObjectInstance, sqlRow.getDate(fieldName));
                        }

                        else {
                            field.set(newObjectInstance, sqlRow.getObject(fieldName));
                        }
                    }
                }
            }
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return newObjectInstance;
    }

    private static boolean isFieldInstanceOfType(Field field, Class<?> targetType) {
        Class<?> fieldType = field.getType();
        return targetType.isAssignableFrom(fieldType);
    }
}
