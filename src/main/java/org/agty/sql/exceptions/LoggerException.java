package org.agty.sql.exceptions;

/**
 * Provides logger exception behavior.
 */
public class LoggerException extends RuntimeException {
    /**
     * Creates a new instance.
     * @param message parameter value
     */
    public LoggerException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     * @param message parameter value
     * @param cause parameter value
     */
    public LoggerException(String message, Throwable cause) {
        super(message, cause);
    }
}
