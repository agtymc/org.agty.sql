package org.agty.sql.driver;

/**
 * Reliability guarantee provided by a dialect read-after-write strategy.
 */
public enum ReadAfterWriteSafety {
    /** The write and returned row are produced by one database statement. */
    ATOMIC,
    /** The value is isolated to the current physical database connection. */
    CONNECTION_SCOPED,
    /** Correctness requires the caller to keep the write and read in one transaction. */
    TRANSACTION_GUARDED,
    /** The fallback may select a different row when concurrent writes occur. */
    COLLISION_PRONE,
    /** The dialect does not provide the operation. */
    UNSUPPORTED;

    /**
     * Whether the strategy can return a different row under concurrent writes.
     *
     * @return {@code true} for a collision-prone fallback
     */
    public boolean isCollisionProne() {
        return this == COLLISION_PRONE;
    }

    /**
     * Whether the documented guarantee requires explicit transaction control.
     *
     * @return {@code true} when a transaction is required
     */
    public boolean requiresTransaction() {
        return this == TRANSACTION_GUARDED;
    }
}
