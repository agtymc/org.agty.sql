package org.agty.sql.model.exceptions;

/**
 * Provides object is not entity exception behavior.
 */
public class ObjectIsNotEntityException extends RuntimeException {
    /**
     * Creates a new instance.
     * @param message parameter value
     */
    public ObjectIsNotEntityException(String message) {
        super(message);
    }
}
