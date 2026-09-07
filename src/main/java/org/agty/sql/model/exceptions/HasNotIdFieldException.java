package org.agty.sql.model.exceptions;

/**
 * Provides has not id field exception behavior.
 */
public class HasNotIdFieldException extends RuntimeException {
    /**
     * Creates a new instance.
     * @param message parameter value
     */
    public HasNotIdFieldException(String message) {
        super(message);
    }
}
