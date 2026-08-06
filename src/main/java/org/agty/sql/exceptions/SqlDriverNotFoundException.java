package org.agty.sql.exceptions;

/**
 * Driver not found exception
 */
public class SqlDriverNotFoundException extends RuntimeException {
    public SqlDriverNotFoundException(String driver) {
        super("AgtySQL Driver [" + driver + "] not found.");
    }
}
