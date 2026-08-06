package org.agty.sql.model.builders;

import org.agty.sql.model.annotations.Column;
import org.agty.sql.model.annotations.Entity;
import org.agty.sql.model.annotations.Id;
import org.agty.sql.model.entity.ColumnEntity;
import org.agty.sql.model.exceptions.HasNotIdFieldException;
import org.agty.sql.model.utils.ModelUtils;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ColumnsBuilder implements ColumnBuilderInterface {
    private boolean idFieldHasPresent = false;
    private String columnName;

    private final Set<ColumnEntity> columns = new HashSet<>();
    private final Set<ColumnBuilderInterface> entities = new HashSet<>();

    /**
     * Build an entity
     * @param entity Entity object
     * @param <T> Type of entity
     * @throws IllegalAccessException
     */
    @Override
    public <T> void buildEntity(T entity) throws IllegalAccessException {
        for (Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            buildColumn(
                    createFieldName(field, field.get(entity)),
                    field,
                    entity
            );
        }

        if (!idFieldHasPresent)
            throw new HasNotIdFieldException("Entity " + entity.getClass().getName() + " has no id field");
    }

    /**
     * Добавить дополнительные поля (столбцы) со значениями
     * @param additionalFields Map of additional fields
     */
    @Override
    public void addAdditionalFields(Map<String, Object> additionalFields) {
        for (String key : additionalFields.keySet()) {
            ColumnEntity columnEntity = new ColumnEntity(
                    key,
                    additionalFields.get(key)
            );

            columns.add(columnEntity);
        }
    }

    /**
     * Create a field name
     * @param field Field object
     * @return Field name
     */
    private String createFieldName(Field field, Object value) {
        boolean annotationColumn = field.isAnnotationPresent(Column.class);
        boolean annotationId = field.isAnnotationPresent(Id.class);

        if (annotationColumn || annotationId) {
            if (annotationColumn && field.getAnnotation(Column.class).skip()) return null;
            if (annotationColumn && field.getAnnotation(Column.class).skipIfNull() && Objects.isNull(value)) return null;
            if (annotationId) idFieldHasPresent = true;

            String fieldName = field.getName();

            if (annotationColumn && !field.getAnnotation(Column.class).name().isBlank()) {
                fieldName = field.getAnnotation(Column.class).name();
            }
            return fieldName;
        }

        return field.getName();
    }

    /**
     * Build a column
     * @param fieldName Field name
     * @param field Field object
     * @param entity Entity object
     * @param <T> Type of entity object
     * @throws IllegalAccessException
     */
    private <T> void buildColumn(String fieldName, Field field, T entity) throws IllegalAccessException {
        if (fieldName == null) return;

        if (ModelUtils.columnIsSimple(field.getType())) {
            ColumnEntity columnEntity = new ColumnEntity(
                    fieldName,
                    field.get(entity)
            );

            columnEntity.setType(field.getType().getSimpleName());

            if (field.isAnnotationPresent(Id.class)) {
                columnEntity.setColumnIsId(true);
                columnEntity.setForceId(field.getAnnotation(Id.class).force());
            }

            columns.add(columnEntity);
        } else {
            if (field.get(entity) != null && field.get(entity).getClass().isAnnotationPresent(Entity.class)) {
                ColumnBuilderInterface columnBuilder = new ColumnsBuilder();
                columnBuilder.setColumnName(fieldName);
                columnBuilder.buildEntity(field.get(entity));
                entities.add(columnBuilder);
            }
        }
    }

    @Override
    public String getColumnName() {
        return columnName;
    }

    @Override
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @Override
    public Set<ColumnEntity> getColumns() {
        return columns;
    }

    @Override
    public Set<ColumnBuilderInterface> getEntities() {
        return entities;
    }

    @Override
    public String toString() {
        return "ColumnsBuilder{" +
                "columnName='" + columnName + '\'' +
                ", columns=" + columns +
                ", entities=" + entities +
                '}';
    }
}
