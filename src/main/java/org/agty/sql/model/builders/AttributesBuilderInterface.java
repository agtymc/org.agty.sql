package org.agty.sql.model.builders;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Provides attributes builder interface behavior.
 */
public interface AttributesBuilderInterface {
    /**
     * Performs the build entity operation.
     * @param <T> type parameter
     * @param entity parameter value
     * @throws IllegalAccessException if the operation cannot be completed
     * @throws InvocationTargetException if the operation cannot be completed
     */
    <T> void buildEntity(T entity) throws IllegalAccessException, InvocationTargetException;
    /**
     * Returns the where condition.
     * @return operation result
     */
    String getWhereCondition();
    /**
     * Returns whether where condition.
     * @return operation result
     */
    boolean hasWhereCondition();
    /**
     * Returns the table name.
     * @return operation result
     */
    String getTableName();
    /**
     * Returns whether table name.
     * @return operation result
     */
    boolean hasTableName();
    /**
     * Returns the schema name.
     * @return operation result
     */
    String getSchemaName();
    /**
     * Returns whether schema name.
     * @return operation result
     */
    boolean hasSchemaName();
    /**
     * Returns the additional fields.
     * @return operation result
     */
    Map<String, Object> getAdditionalFields();
    /**
     * Returns whether additional fields.
     * @return operation result
     */
    boolean hasAdditionalFields();
}
