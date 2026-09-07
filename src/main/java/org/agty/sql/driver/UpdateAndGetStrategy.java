package org.agty.sql.driver;

/**
 * Strategy used by a dialect to return a row after an UPDATE operation.
 */
public enum UpdateAndGetStrategy {
    /**
     * Defines the none value.
     */
    NONE,
    /**
     * Defines the native returning value.
     */
    NATIVE_RETURNING,
    /**
     * Defines the follow up fetch by primary key value.
     */
    FOLLOW_UP_FETCH_BY_PRIMARY_KEY,
    /**
     * Defines the follow up fetch by where value.
     */
    FOLLOW_UP_FETCH_BY_WHERE,
    /**
     * Defines the follow up fetch unsafe value.
     */
    FOLLOW_UP_FETCH_UNSAFE;

    /**
     * Performs the uses follow up fetch operation.
     * @return operation result
     */
    public boolean usesFollowUpFetch() {
        return this == FOLLOW_UP_FETCH_BY_PRIMARY_KEY
                || this == FOLLOW_UP_FETCH_BY_WHERE
                || this == FOLLOW_UP_FETCH_UNSAFE;
    }

    /**
     * Returns whether collision prone.
     * @return operation result
     */
    public boolean isCollisionProne() {
        return this == FOLLOW_UP_FETCH_UNSAFE;
    }
}
