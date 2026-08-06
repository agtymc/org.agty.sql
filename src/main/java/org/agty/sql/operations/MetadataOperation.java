package org.agty.sql.operations;

import org.agty.sql.AgtySqlOperationSupport;
import org.agty.sql.data.Arguments;
import org.agty.sql.interfaces.SqlRow;

public final class MetadataOperation {

    private final AgtySqlOperationSupport support;

    public MetadataOperation(AgtySqlOperationSupport support) {
        this.support = support;
    }

    public boolean truncate(Arguments arguments) {
        String query = support.getDriverSqlObject()
                .truncateQuery(support.rebuildTable(arguments.getTable()));
        support.debugMessage("AgtySQL.truncate()", "Query: " + query);
        return support.execute(query, arguments.noRebuildQuery());
    }

    public boolean dropColumn(Arguments arguments) {
        String query = support.getDriverSqlObject().dropColumnQuery(arguments);
        support.debugMessage("AgtySQL.dropColumn()", "Query: " + query);
        return support.execute(query, arguments.noRebuildQuery());
    }

    public boolean dropTable(Arguments arguments) {
        String query = support.getDriverSqlObject()
                .dropTableQuery(support.rebuildTable(arguments.getTable()));
        support.debugMessage("AgtySQL.dropTable()", "Query: " + query);
        return support.execute(query, arguments.noRebuildQuery());
    }

    public Long max(Arguments arguments) {
        return support.getDriverSqlObject().max(arguments);
    }

    public Long maxOrDefault(Arguments arguments, long defaultValue) {
        Long max = max(arguments);
        return max == null ? defaultValue : max;
    }

    public Long min(Arguments arguments) {
        return support.getDriverSqlObject().min(arguments);
    }

    public Long minOrDefault(Arguments arguments, long defaultValue) {
        Long min = min(arguments);
        return min == null ? defaultValue : min;
    }

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
