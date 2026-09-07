package org.agty.sql.driver;

/**
 * Declares the behavior guarantees of a concrete SQL dialect.
 *
 * @param fileBased whether the dialect stores a database in a local file
 * @param supportsSchema whether the dialect supports named schemas
 * @param lastInsertIdStrategy strategy used to read a generated identifier
 * @param insertAndGetStrategy strategy used to return an inserted row
 * @param updateAndGetStrategy strategy used to return an updated row
 */
public record DialectCapabilities(
        boolean fileBased,
        boolean supportsSchema,
        LastInsertIdStrategy lastInsertIdStrategy,
        WriteReturnStrategy insertAndGetStrategy,
        UpdateAndGetStrategy updateAndGetStrategy
) {

    /**
     * Performs the of operation.
     * @param fileBased parameter value
     * @param supportsSchema parameter value
     * @param lastInsertIdStrategy parameter value
     * @param insertAndGetStrategy parameter value
     * @param updateAndGetStrategy parameter value
     * @return operation result
     */
    public static DialectCapabilities of(
            boolean fileBased,
            boolean supportsSchema,
            LastInsertIdStrategy lastInsertIdStrategy,
            WriteReturnStrategy insertAndGetStrategy,
            UpdateAndGetStrategy updateAndGetStrategy
    ) {
        return new DialectCapabilities(
                fileBased,
                supportsSchema,
                lastInsertIdStrategy,
                insertAndGetStrategy,
                updateAndGetStrategy
        );
    }

    /**
     * Performs the none operation.
     * @return operation result
     */
    public static DialectCapabilities none() {
        return of(
                false,
                false,
                LastInsertIdStrategy.NONE,
                WriteReturnStrategy.NONE,
                UpdateAndGetStrategy.NONE
        );
    }

    /**
     * Returns whether insert and get.
     * @return operation result
     */
    public boolean supportsInsertAndGet() {
        return insertAndGetStrategy != WriteReturnStrategy.NONE;
    }

    /**
     * Returns whether insert and get returning.
     * @return operation result
     */
    public boolean supportsInsertAndGetReturning() {
        return insertAndGetStrategy == WriteReturnStrategy.NATIVE_RETURNING;
    }

    /**
     * Performs the uses follow up fetch for insert and get operation.
     * @return operation result
     */
    public boolean usesFollowUpFetchForInsertAndGet() {
        return insertAndGetStrategy == WriteReturnStrategy.FOLLOW_UP_FETCH;
    }

    /**
     * Returns whether update and get.
     * @return operation result
     */
    public boolean supportsUpdateAndGet() {
        return updateAndGetStrategy != UpdateAndGetStrategy.NONE;
    }

    /**
     * Returns whether update and get returning.
     * @return operation result
     */
    public boolean supportsUpdateAndGetReturning() {
        return updateAndGetStrategy == UpdateAndGetStrategy.NATIVE_RETURNING;
    }

    /**
     * Performs the uses follow up fetch for update and get operation.
     * @return operation result
     */
    public boolean usesFollowUpFetchForUpdateAndGet() {
        return updateAndGetStrategy.usesFollowUpFetch();
    }

    /**
     * Performs the uses primary key follow up for update and get operation.
     * @return operation result
     */
    public boolean usesPrimaryKeyFollowUpForUpdateAndGet() {
        return updateAndGetStrategy == UpdateAndGetStrategy.FOLLOW_UP_FETCH_BY_PRIMARY_KEY;
    }

    /**
     * Performs the uses where follow up for update and get operation.
     * @return operation result
     */
    public boolean usesWhereFollowUpForUpdateAndGet() {
        return updateAndGetStrategy == UpdateAndGetStrategy.FOLLOW_UP_FETCH_BY_WHERE;
    }

    /**
     * Performs the uses unsafe follow up for update and get operation.
     * @return operation result
     */
    public boolean usesUnsafeFollowUpForUpdateAndGet() {
        return updateAndGetStrategy == UpdateAndGetStrategy.FOLLOW_UP_FETCH_UNSAFE;
    }

    /**
     * Returns whether last insert id.
     * @return operation result
     */
    public boolean supportsLastInsertId() {
        return lastInsertIdStrategy != LastInsertIdStrategy.NONE;
    }

    /**
     * Performs the uses unsafe last insert id fallback operation.
     * @return operation result
     */
    public boolean usesUnsafeLastInsertIdFallback() {
        return lastInsertIdStrategy == LastInsertIdStrategy.FETCH_LAST_ROW_UNSAFE;
    }

    /**
     * Returns the reliability guarantee of generated-ID resolution.
     *
     * @return generated-ID safety level
     */
    public ReadAfterWriteSafety lastInsertIdSafety() {
        return switch (lastInsertIdStrategy) {
            case NONE -> ReadAfterWriteSafety.UNSUPPORTED;
            case CONNECTION_FUNCTION, SEQUENCE_FUNCTION -> ReadAfterWriteSafety.CONNECTION_SCOPED;
            case FETCH_LAST_ROW_UNSAFE -> ReadAfterWriteSafety.COLLISION_PRONE;
        };
    }

    /**
     * Returns the reliability guarantee of {@code insertAndGet}.
     *
     * <p>A follow-up read should be kept in the same explicit transaction even
     * when the generated identifier itself is connection-scoped.</p>
     *
     * @return insert-return safety level
     */
    public ReadAfterWriteSafety insertAndGetSafety() {
        return switch (insertAndGetStrategy) {
            case NONE -> ReadAfterWriteSafety.UNSUPPORTED;
            case NATIVE_RETURNING -> ReadAfterWriteSafety.ATOMIC;
            case FOLLOW_UP_FETCH -> usesUnsafeLastInsertIdFallback()
                    ? ReadAfterWriteSafety.COLLISION_PRONE
                    : ReadAfterWriteSafety.TRANSACTION_GUARDED;
        };
    }

    /**
     * Returns the reliability guarantee of {@code updateAndGet}.
     *
     * @return update-return safety level
     */
    public ReadAfterWriteSafety updateAndGetSafety() {
        return switch (updateAndGetStrategy) {
            case NONE -> ReadAfterWriteSafety.UNSUPPORTED;
            case NATIVE_RETURNING -> ReadAfterWriteSafety.ATOMIC;
            case FOLLOW_UP_FETCH_BY_PRIMARY_KEY -> ReadAfterWriteSafety.TRANSACTION_GUARDED;
            case FOLLOW_UP_FETCH_BY_WHERE, FOLLOW_UP_FETCH_UNSAFE ->
                    ReadAfterWriteSafety.COLLISION_PRONE;
        };
    }
}
