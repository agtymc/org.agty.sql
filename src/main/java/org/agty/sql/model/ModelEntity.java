package org.agty.sql.model;

import org.agty.sql.model.builders.AttributesBuilderInterface;
import org.agty.sql.model.builders.ColumnBuilderInterface;
import org.agty.sql.model.builders.TableBuilderInterface;

import java.lang.reflect.InvocationTargetException;

public class ModelEntity {
    private final TableBuilderInterface tableBuilder ;
    private final ColumnBuilderInterface columnsBuilder;
    private final AttributesBuilderInterface attributesBuilderInterface;

    public ModelEntity(TableBuilderInterface tableBuilder, ColumnBuilderInterface columnsBuilder, AttributesBuilderInterface attributesBuilderInterface) {
        this.tableBuilder = tableBuilder;
        this.columnsBuilder = columnsBuilder;
        this.attributesBuilderInterface = attributesBuilderInterface;
    }

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

    public String getTableName() {
        return tableBuilder.getTableName();
    }

    public TableBuilderInterface getTableBuilder() {
        return tableBuilder;
    }

    public ColumnBuilderInterface getColumnsBuilder() {
        return columnsBuilder;
    }

    public AttributesBuilderInterface getAttributesBuilder() {
        return attributesBuilderInterface;
    }
}
