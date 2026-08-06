package org.agty.sql.driver;

/**
 * Strategy used by a dialect to resolve the identifier of the row inserted in
 * the current high-level flow.
 */
public enum LastInsertIdStrategy {
    NONE,
    CONNECTION_FUNCTION,
    SEQUENCE_FUNCTION,
    FETCH_LAST_ROW_UNSAFE
}
