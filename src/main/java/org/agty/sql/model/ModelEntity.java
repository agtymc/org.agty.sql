package org.agty.sql.model;

import org.agty.sql.model.builders.AttributesBuilderInterface;
import org.agty.sql.model.builders.ColumnBuilderInterface;
import org.agty.sql.model.builders.TableBuilderInterface;

import java.lang.reflect.InvocationTargetException;

/**
 * Provides model entity behavior.
 */
public class ModelEntity {
    private final TableBuilderInterface tableBuilder ;
    private final ColumnBuilderInterface columnsBuilder;
    private final AttributesBuilderInterface attributesBuilderInterface;

    /**
     * Creates a new instance.
     * @param tableBuilder parameter value
     * @param columnsBuilder parameter value
     * @param attributesBuilderInterface parameter value
     */
    public ModelEntity(TableBuilderInterface tableBuilder, ColumnBuilderInterface columnsBuilder, AttributesBuilderInterface attributesBuilderInterface) {
        this.tableBuilder = tableBuilder;
        this.columnsBuilder = columnsBuilder;
        this.attributesBuilderInterface = attributesBuilderInterface;
    }

    /**
     * Performs the build entity operation.
     * @param <T> type parameter
     * @param entity parameter value
     */
    public <T> void buildEntity(T entity) {
        //Сначала атрибуты
        try {
            attributesBuilderInterface.buildEntity(entity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to build model attributes", e);
        }

        //Потом колонки
        try {
            columnsBuilder.buildEntity(entity);
            if (attributesBuilderInterface.hasAdditionalFields()) {
                columnsBuilder.addAdditionalFields(attributesBuilderInterface.getAdditionalFields());
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to build model columns", e);
        }

        //И только затем имя таблицы
        if (attributesBuilderInterface.hasTableName()) {
            String schemaName = attributesBuilderInterface.hasSchemaName() ? attributesBuilderInterface.getSchemaName() : null;
            tableBuilder.setTableName(attributesBuilderInterface.getTableName(), schemaName);
        }
        else {
            tableBuilder.buildTableName(entity);
        }

    }

    /**
     * Returns the table name.
     * @return operation result
     */
    public String getTableName() {
        return tableBuilder.getTableName();
    }

    /**
     * Returns the table builder.
     * @return operation result
     */
    public TableBuilderInterface getTableBuilder() {
        return tableBuilder;
    }

    /**
     * Returns the columns builder.
     * @return operation result
     */
    public ColumnBuilderInterface getColumnsBuilder() {
        return columnsBuilder;
    }

    /**
     * Returns the attributes builder.
     * @return operation result
     */
    public AttributesBuilderInterface getAttributesBuilder() {
        return attributesBuilderInterface;
    }
}
