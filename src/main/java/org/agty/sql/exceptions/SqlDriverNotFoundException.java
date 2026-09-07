package org.agty.sql.exceptions;

/**
 * Driver not found exception
 */
public class SqlDriverNotFoundException extends RuntimeException {
    /**
     * Creates a new instance.
     * @param driver parameter value
     */
    public SqlDriverNotFoundException(String driver) {
        super("AgtySQL Driver [" + driver + "] not found.");
    }
}
