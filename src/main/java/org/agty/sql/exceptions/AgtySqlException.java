package org.agty.sql.exceptions;

/**
 * Default SQL exception
 */
public class AgtySqlException extends RuntimeException {
    public AgtySqlException(String type, String message) {
        super(type + " -> " + message);
    }

    public AgtySqlException(String type, String message, Throwable cause) {
        super(type + " -> " + message, cause);
    }
}
