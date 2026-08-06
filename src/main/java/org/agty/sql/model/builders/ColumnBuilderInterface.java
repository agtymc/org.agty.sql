package org.agty.sql.model.builders;

import org.agty.sql.model.entity.ColumnEntity;

import java.util.Map;
import java.util.Set;

public interface ColumnBuilderInterface {
    <T> void buildEntity(T entity) throws IllegalAccessException;
    Set<ColumnEntity> getColumns();
    void setColumnName(String columnName);
    String getColumnName();
    Set<ColumnBuilderInterface> getEntities();
    void addAdditionalFields(Map<String, Object> additionalFields);
}
