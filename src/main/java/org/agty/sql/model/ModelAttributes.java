package org.agty.sql.model;

import org.agty.sql.model.annotations.Entity;
import org.agty.sql.model.builders.AttributesBuilder;
import org.agty.sql.model.builders.ColumnsBuilder;
import org.agty.sql.model.builders.TableBuilder;
import org.agty.sql.model.exceptions.ObjectIsNotEntityException;

public class ModelAttributes <T> {
    private final T entity;
    private ModelEntity model = new ModelEntity(new TableBuilder(), new ColumnsBuilder(), new AttributesBuilder());

    public ModelAttributes(T entity) {
        if (!entity.getClass().isAnnotationPresent(Entity.class))
            throw new ObjectIsNotEntityException("Class " + entity.getClass().getName() + " is not annotated with @Entity");

        this.entity = entity;
    }

    public ModelAttributes <T> build() {
        model.buildEntity(entity);
        return this;
    }

    public ModelEntity getModel() {
        return model;
    }
}
