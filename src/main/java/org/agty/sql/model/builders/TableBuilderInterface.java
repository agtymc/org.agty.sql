package org.agty.sql.model.builders;

public interface TableBuilderInterface {
    <T> void buildTableName(T entity);
    String getTableName();
    void setTableName(String tableName, String schemaName);
}
