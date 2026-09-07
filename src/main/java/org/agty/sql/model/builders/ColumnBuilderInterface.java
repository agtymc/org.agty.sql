package org.agty.sql.model.builders;

import org.agty.sql.model.entity.ColumnEntity;

import java.util.Map;
import java.util.Set;

/**
 * Provides column builder interface behavior.
 */
public interface ColumnBuilderInterface {
    /**
     * Performs the build entity operation.
     * @param <T> type parameter
     * @param entity parameter value
     * @throws IllegalAccessException if the operation cannot be completed
     */
    <T> void buildEntity(T entity) throws IllegalAccessException;
    /**
     * Returns the columns.
     * @return operation result
     */
    Set<ColumnEntity> getColumns();
    /**
     * Sets the column name.
     * @param columnName parameter value
     */
    void setColumnName(String columnName);
    /**
     * Returns the column name.
     * @return operation result
     */
    String getColumnName();
    /**
     * Returns the entities.
     * @return operation result
     */
    Set<ColumnBuilderInterface> getEntities();
    /**
     * Adds the additional fields.
     * @param additionalFields parameter value
     */
    void addAdditionalFields(Map<String, Object> additionalFields);
}
