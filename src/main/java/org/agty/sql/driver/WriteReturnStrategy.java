package org.agty.sql.driver;

/**
 * Strategy used by a dialect to return a row after a write operation.
 */
public enum WriteReturnStrategy {
    NONE,
    NATIVE_RETURNING,
    FOLLOW_UP_FETCH
}
