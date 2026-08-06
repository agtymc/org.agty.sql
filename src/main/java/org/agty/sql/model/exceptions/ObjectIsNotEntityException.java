package org.agty.sql.model.exceptions;

public class ObjectIsNotEntityException extends RuntimeException {
    public ObjectIsNotEntityException(String message) {
        super(message);
    }
}
