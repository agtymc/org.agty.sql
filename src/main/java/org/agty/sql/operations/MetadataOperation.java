package org.agty.sql.operations;

import org.agty.sql.AgtySqlOperationSupport;
import org.agty.sql.data.Arguments;
import org.agty.sql.interfaces.SqlRow;

/**
 * Provides metadata operation behavior.
 */
public final class MetadataOperation {

    private final AgtySqlOperationSupport support;

    /**
     * Creates a new instance.
     * @param support parameter value
     */
    public MetadataOperation(AgtySqlOperationSupport support) {
        this.support = support;
    }

    /**
     * Performs the truncate operation.
     * @param arguments parameter value
     * @return operation result
     */
    public boolean truncate(Arguments arguments) {
        String query = support.getDriverSqlObject()
                .truncateQuery(support.rebuildTable(arguments.getTable()));
        support.debugMessage("AgtySQL.truncate()", "Query: " + query);
        return support.execute(query, arguments.noRebuildQuery());
    }

    /**
     * Performs the drop column operation.
     * @param arguments parameter value
     * @return operation result
     */
    public boolean dropColumn(Arguments arguments) {
        String query = support.getDriverSqlObject().dropColumnQuery(arguments);
        support.debugMessage("AgtySQL.dropColumn()", "Query: " + query);
        return support.execute(query, arguments.noRebuildQuery());
    }

    /**
     * Performs the drop table operation.
     * @param arguments parameter value
     * @return operation result
     */
    public boolean dropTable(Arguments arguments) {
        String query = support.getDriverSqlObject()
                .dropTableQuery(support.rebuildTable(arguments.getTable()));
        support.debugMessage("AgtySQL.dropTable()", "Query: " + query);
        return support.execute(query, arguments.noRebuildQuery());
    }

    /**
     * Performs the max operation.
     * @param arguments parameter value
     * @return operation result
     */
    public Long max(Arguments arguments) {
        return support.getDriverSqlObject().max(arguments);
    }

    /**
     * Performs the max or default operation.
     * @param arguments parameter value
     * @param defaultValue parameter value
     * @return operation result
     */
    public Long maxOrDefault(Arguments arguments, long defaultValue) {
        Long max = max(arguments);
        if (max != null) return max;
        return defaultValue;
    }

    /**
     * Performs the min operation.
     * @param arguments parameter value
     * @return operation result
     */
    public Long min(Arguments arguments) {
        return support.getDriverSqlObject().min(arguments);
    }

    /**
     * Performs the min or default operation.
     * @param arguments parameter value
     * @param defaultValue parameter value
     * @return operation result
     */
    public Long minOrDefault(Arguments arguments, long defaultValue) {
        Long min = min(arguments);
        if (min != null) return min;
        return defaultValue;
    }

    /**
     * Performs the last insert id operation.
     * @param arguments parameter value
     * @return operation result
     */
    public Long lastInsertId(Arguments arguments) {
        return switch (support.getDialectCapabilities().lastInsertIdStrategy()) {
            case NONE -> null;
            case CONNECTION_FUNCTION -> support.getDriverSqlObject().getLastInsertId(
                    arguments.hasTable() ? support.rebuildTable(arguments.getTable()) : null
            );
            case SEQUENCE_FUNCTION -> getLastInsertIdBySequence(arguments);
            case FETCH_LAST_ROW_UNSAFE -> fetchLastInsertedIdUnsafe(arguments);
        };
    }

    private Long getLastInsertIdBySequence(Arguments arguments) {
        if (!arguments.hasTable()) {
            support.throwError(
                    "AgtySQL.lastInsertId()",
                    "Table metadata is required for sequence-based lastInsertId()"
            );
            return null;
        }

        return support.getDriverSqlObject().getLastInsertId(
                support.rebuildTable(arguments.getTable())
        );
    }

    private Long fetchLastInsertedIdUnsafe(Arguments arguments) {
        String primaryKey = support.getDriverSqlObject().getPrimaryKey(arguments);

        if (primaryKey == null || primaryKey.isEmpty()) {
            support.throwError("AgtySQL.lastInsertId()", "Primary key is required for last-row fallback");
            return null;
        }

        support.debugMessage(
                "AgtySQL.lastInsertId()",
                "Using collision-prone fallback FETCH_LAST_ROW_UNSAFE for driver '%s'".formatted(
                        support.getConfig().getDriver()
                )
        );

        SqlRow lastRow = support.fetch(
                Arguments.builder()
                        .setTable(arguments.getTable())
                        .setPrimaryKey(primaryKey)
                        .setActionField(primaryKey)
                        .setFields(primaryKey)
        );

        return lastRow == null || lastRow.isEmpty() ? null : lastRow.getLong(primaryKey);
    }
}
