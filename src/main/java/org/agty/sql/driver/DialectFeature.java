package org.agty.sql.driver;

/**
 * Database or JDBC capability that can vary between supported dialects.
 *
 * <p>These values describe available building blocks. They do not imply that
 * every feature has a dedicated high-level {@code AgtySQL} operation.</p>
 */
public enum DialectFeature {
    /** Returning deleted rows from the same DML statement. */
    DELETE_RETURNING,
    /** Atomic insert-or-update syntax provided by the database engine. */
    UPSERT,
    /** A locking read equivalent to {@code SELECT ... FOR UPDATE}. */
    SELECT_FOR_UPDATE,
    /** JDBC statement batching. */
    JDBC_BATCH,
    /** JDBC generated-key retrieval. */
    JDBC_GENERATED_KEYS,
    /** JDBC positional {@code ?} parameters. */
    POSITIONAL_PARAMETERS,
    /** Named query parameters handled directly by the driver or dialect. */
    NAMED_PARAMETERS
}
