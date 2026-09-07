package org.agty.sql.model.builders;

/**
 * Provides table builder interface behavior.
 */
public interface TableBuilderInterface {
    /**
     * Performs the build table name operation.
     * @param <T> type parameter
     * @param entity parameter value
     */
    <T> void buildTableName(T entity);
    /**
     * Returns the table name.
     * @return operation result
     */
    String getTableName();
    /**
     * Sets the table name.
     * @param tableName parameter value
     * @param schemaName parameter value
     */
    void setTableName(String tableName, String schemaName);
}
