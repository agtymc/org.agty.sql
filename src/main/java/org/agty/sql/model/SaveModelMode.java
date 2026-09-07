package org.agty.sql.model;

/**
 * Provides save model mode behavior.
 */
public enum SaveModelMode {
    /**
     * Inserts or updates after checking whether the matching row exists.
     */
    WITH_CHECK,
    /**
     * Updates when a WHERE condition exists and inserts otherwise, without an existence query.
     */
    WITHOUT_CHECK,
    /**
     * Inserts a new model without an existence query.
     */
    INSERT_ONLY,
    /**
     * Inserts only when an existence query does not find a matching row.
     */
    INSERT_ONLY_WITH_CHECK,
    /**
     * Updates only when a WHERE condition is available.
     */
    UPDATE_ONLY,
    /**
     * Legacy update-with-check mode retained for {@code 2.x} compatibility.
     */
    UPDATE_ONLY_WITH_CHECK,
    /**
     * Inserts when no matching row exists and otherwise skips the operation.
     */
    SAVE_OR_SKIP;
}
