package org.agty.sql.model;

import org.agty.sql.model.annotations.Entity;
import org.agty.sql.model.builders.AttributesBuilder;
import org.agty.sql.model.builders.ColumnsBuilder;
import org.agty.sql.model.builders.TableBuilder;
import org.agty.sql.model.exceptions.ObjectIsNotEntityException;

/**
 * Provides model attributes behavior.
 * @param <T> type parameter
 */
public class ModelAttributes <T> {
    private final T entity;
    private ModelEntity model = new ModelEntity(new TableBuilder(), new ColumnsBuilder(), new AttributesBuilder());

    /**
     * Creates a new instance.
     * @param entity parameter value
     */
    public ModelAttributes(T entity) {
        if (!entity.getClass().isAnnotationPresent(Entity.class))
            throw new ObjectIsNotEntityException("Class " + entity.getClass().getName() + " is not annotated with @Entity");

        this.entity = entity;
    }

    /**
     * Performs the build operation.
     * @return operation result
     */
    public ModelAttributes <T> build() {
        model.buildEntity(entity);
        return this;
    }

    /**
     * Returns the model.
     * @return operation result
     */
    public ModelEntity getModel() {
        return model;
    }
}
