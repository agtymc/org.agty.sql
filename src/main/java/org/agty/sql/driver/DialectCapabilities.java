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

    public static DialectCapabilities none() {
        return of(
                false,
                false,
                LastInsertIdStrategy.NONE,
                WriteReturnStrategy.NONE,
                UpdateAndGetStrategy.NONE
        );
    }

    public boolean supportsInsertAndGet() {
        return insertAndGetStrategy != WriteReturnStrategy.NONE;
    }

    public boolean supportsInsertAndGetReturning() {
        return insertAndGetStrategy == WriteReturnStrategy.NATIVE_RETURNING;
    }

    public boolean usesFollowUpFetchForInsertAndGet() {
        return insertAndGetStrategy == WriteReturnStrategy.FOLLOW_UP_FETCH;
    }

    public boolean supportsUpdateAndGet() {
        return updateAndGetStrategy != UpdateAndGetStrategy.NONE;
    }

    public boolean supportsUpdateAndGetReturning() {
        return updateAndGetStrategy == UpdateAndGetStrategy.NATIVE_RETURNING;
    }

    public boolean usesFollowUpFetchForUpdateAndGet() {
        return updateAndGetStrategy.usesFollowUpFetch();
    }

    public boolean usesPrimaryKeyFollowUpForUpdateAndGet() {
        return updateAndGetStrategy == UpdateAndGetStrategy.FOLLOW_UP_FETCH_BY_PRIMARY_KEY;
    }

    public boolean usesWhereFollowUpForUpdateAndGet() {
        return updateAndGetStrategy == UpdateAndGetStrategy.FOLLOW_UP_FETCH_BY_WHERE;
    }

    public boolean usesUnsafeFollowUpForUpdateAndGet() {
        return updateAndGetStrategy == UpdateAndGetStrategy.FOLLOW_UP_FETCH_UNSAFE;
    }

    public boolean supportsLastInsertId() {
        return lastInsertIdStrategy != LastInsertIdStrategy.NONE;
    }

    public boolean usesUnsafeLastInsertIdFallback() {
        return lastInsertIdStrategy == LastInsertIdStrategy.FETCH_LAST_ROW_UNSAFE;
    }
}
