package org.agty.sql.model.builders;

import org.agty.sql.model.annotations.AdditionalFields;
import org.agty.sql.model.annotations.SchemaName;
import org.agty.sql.model.annotations.TableName;
import org.agty.sql.model.annotations.WhereCondition;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class AttributesBuilder implements AttributesBuilderInterface {
    private String whereCondition;
    private String tableName;
    private String schemaName;
    private final Map<String, Object> additionalFields = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> void buildEntity(T entity) throws IllegalAccessException, InvocationTargetException {
        for (Method method : entity.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(WhereCondition.class)) {
                method.setAccessible(true);
                Object result = method.invoke(entity);
                if (result != null) setWhereCondition(result.toString());
            }

            if (method.isAnnotationPresent(TableName.class)) {
                method.setAccessible(true);
                Object result = method.invoke(entity);
                if (result != null) setTableName(result.toString());
            }

            if (method.isAnnotationPresent(SchemaName.class)) {
                method.setAccessible(true);
                Object result = method.invoke(entity);
                if (result != null) setSchemaName(result.toString());
            }

            if (method.isAnnotationPresent(AdditionalFields.class)) {
                method.setAccessible(true);
                Object result = method.invoke(entity);
                if (result != null) setAdditionalFields((Map<String, Object>) result);
            }
        }
    }

    private void setWhereCondition(String whereCondition) {
        if (!hasWhereCondition()) this.whereCondition = whereCondition;
    }

    private void setTableName(String tableName) {
        if (!hasTableName()) this.tableName = tableName;
    }

    private void setSchemaName(String schemaName) {
        if (!hasSchemaName()) this.schemaName = schemaName;
    }

    private void setAdditionalFields(Map<String, Object> additionalFields) {
        if (!hasAdditionalFields()) {
            this.additionalFields.putAll(additionalFields);
        }
    }
    
    @Override
    public String getWhereCondition() {
        return whereCondition;
    }

    @Override
    public boolean hasWhereCondition() {
        return this.whereCondition != null && !this.whereCondition.isBlank();
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public boolean hasTableName() {
        return tableName != null && !tableName.isBlank();
    }

    @Override
    public String getSchemaName() {
        return schemaName;
    }

    @Override
    public boolean hasSchemaName() {
        return schemaName != null && !schemaName.isBlank();
    }

    @Override
    public Map<String, Object> getAdditionalFields() {
        return additionalFields;
    }

    @Override
    public boolean hasAdditionalFields() {
        return !additionalFields.isEmpty();
    }
}
