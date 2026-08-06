package org.agty.sql.model.builders;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public interface AttributesBuilderInterface {
    <T> void buildEntity(T entity) throws IllegalAccessException, InvocationTargetException;
    String getWhereCondition();
    boolean hasWhereCondition();
    String getTableName();
    boolean hasTableName();
    String getSchemaName();
    boolean hasSchemaName();
    Map<String, Object> getAdditionalFields();
    boolean hasAdditionalFields();
}
