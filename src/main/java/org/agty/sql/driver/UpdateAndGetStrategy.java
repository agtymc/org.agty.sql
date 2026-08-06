package org.agty.sql.driver;

/**
 * Strategy used by a dialect to return a row after an UPDATE operation.
 */
public enum UpdateAndGetStrategy {
    NONE,
    NATIVE_RETURNING,
    FOLLOW_UP_FETCH_BY_PRIMARY_KEY,
    FOLLOW_UP_FETCH_BY_WHERE,
    FOLLOW_UP_FETCH_UNSAFE;

    public boolean usesFollowUpFetch() {
        return this == FOLLOW_UP_FETCH_BY_PRIMARY_KEY
                || this == FOLLOW_UP_FETCH_BY_WHERE
                || this == FOLLOW_UP_FETCH_UNSAFE;
    }

    public boolean isCollisionProne() {
        return this == FOLLOW_UP_FETCH_UNSAFE;
    }
}
