package org.agty.sql.driver;

/**
 * Strategy used by a dialect to resolve the identifier of the row inserted in
 * the current high-level flow.
 */
public enum LastInsertIdStrategy {
    /**
     * Defines the none value.
     */
    NONE,
    /**
     * Defines the connection function value.
     */
    CONNECTION_FUNCTION,
    /**
     * Defines the sequence function value.
     */
    SEQUENCE_FUNCTION,
    /**
     * Defines the fetch last row unsafe value.
     */
    FETCH_LAST_ROW_UNSAFE
}
