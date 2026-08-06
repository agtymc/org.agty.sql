package org.agty.sql.exceptions;

/**
 * Default SQL exception
 */
public class AgtySqlException extends RuntimeException {
    public AgtySqlException(String type, String message) {
        super(type + " -> " + message);
    }
}
