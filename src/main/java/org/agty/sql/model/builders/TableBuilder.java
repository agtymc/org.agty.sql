package org.agty.sql.model.builders;

import org.agty.sql.model.annotations.Table;

import java.util.Locale;

/**
 * Provides table builder behavior.
 */
public class TableBuilder implements TableBuilderInterface {
    /** Creates a new TableBuilder instance. */
    public TableBuilder() {
    }

    private String tableName;

    @Override
    public <T> void buildTableName(T entity) {
        Class<?> clazz = entity.getClass();

        if (clazz.isAnnotationPresent(Table.class)) {
            tableName = clazz.getAnnotation(Table.class).name();

            if (!clazz.getAnnotation(Table.class).schema().isBlank()) {
                tableName = clazz.getAnnotation(Table.class).schema() + "." + tableName;
            }

        } else {
            tableName = clazz.getSimpleName().toLowerCase(Locale.ROOT);
        }
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public void setTableName(String tableName, String schemaName) {
        if (schemaName != null && !schemaName.isBlank()) this.tableName = schemaName + "." + tableName;
        else this.tableName = tableName;
    }
}
