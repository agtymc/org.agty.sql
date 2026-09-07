package org.agty.sql.driver;

/**
 * Strategy used by a dialect to return a row after a write operation.
 */
public enum WriteReturnStrategy {
    /**
     * Defines the none value.
     */
    NONE,
    /**
     * Defines the native returning value.
     */
    NATIVE_RETURNING,
    /**
     * Defines the follow up fetch value.
     */
    FOLLOW_UP_FETCH
}
