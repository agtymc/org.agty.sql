package org.agty.sql.exceptions;

/**
 * Default SQL exception
 */
public class AgtySqlException extends RuntimeException {
    /**
     * Creates a new instance.
     * @param type parameter value
     * @param message parameter value
     */
    public AgtySqlException(String type, String message) {
        super(type + " -> " + message);
    }

    /**
     * Creates a new instance.
     * @param type parameter value
     * @param message parameter value
     * @param cause parameter value
     */
    public AgtySqlException(String type, String message, Throwable cause) {
        super(type + " -> " + message, cause);
    }
}
