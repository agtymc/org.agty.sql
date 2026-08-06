package org.agty.sql.operations;

import org.agty.sql.AgtySqlOperationSupport;
import org.agty.sql.data.Arguments;
import org.agty.sql.driver.UpdateAndGetStrategy;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.support.RowFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class UpdateOperation {

    private final AgtySqlOperationSupport support;

    public UpdateOperation(AgtySqlOperationSupport support) {
        this.support = support;
    }

    public boolean update(Arguments arguments) {
        String query = support.hasQuery(arguments)
                ? arguments.getQuery()
                : support.getDriverSqlObject().updateQuery(arguments);

        if (support.hasQuery(query)) {
            support.executeUpdate(query);
        } else {
            support.throwError("AgtySQL.update()", "No a query for UPDATE");
        }

        return !support.hasErrors();
    }

    public SqlRow updateAndGet(Arguments arguments, String fields) {
        UpdateAndGetStrategy strategy = support.getDialectCapabilities().updateAndGetStrategy();
        if (!support.getDialectCapabilities().supportsUpdateAndGet()) {
            support.throwError(
                    "AgtySQL.updateAndGet()",
                    "Driver '%s' does not support updateAndGet()".formatted(support.getConfig().getDriver())
            );
            return RowFactory.emptyRow();
        }

        if (strategy == UpdateAndGetStrategy.NATIVE_RETURNING) {
            return updateAndGetReturning(arguments, fields);
        }

        if (strategy.usesFollowUpFetch()) {
            return updateAndGetFollowUpFetch(arguments, fields, strategy);
        }

        support.throwError(
                "AgtySQL.updateAndGet()",
                "Driver '%s' does not have a documented updateAndGet() strategy".formatted(
                        support.getConfig().getDriver()
                )
        );
        return RowFactory.emptyRow();
    }

    private SqlRow updateAndGetReturning(Arguments arguments, String fields) {

        ResultSet resultSet = support.getDriverSqlObject().updateAndGet(arguments, fields);

        try {
            return support.getFetchRow(resultSet, arguments);
        } catch (SQLException e) {
            support.throwError("AgtySQL.updateAndGet()", e.getMessage());
        }

        return RowFactory.emptyRow();
    }

    private SqlRow updateAndGetFollowUpFetch(Arguments arguments, String fields, UpdateAndGetStrategy strategy) {
        if (!arguments.hasTable()) {
            support.throwError(
                    "AgtySQL.updateAndGet()",
                    "Strategy '%s' for driver '%s' requires table metadata".formatted(
                            strategy, support.getConfig().getDriver())
            );
            return RowFactory.emptyRow();
        }

        if (!arguments.hasWhere()) {
            support.throwError(
                    "AgtySQL.updateAndGet()",
                    "Strategy '%s' for driver '%s' requires WHERE metadata".formatted(
                            strategy, support.getConfig().getDriver())
            );
            return RowFactory.emptyRow();
        }

        if (strategy == UpdateAndGetStrategy.FOLLOW_UP_FETCH_BY_PRIMARY_KEY
                && (arguments.getPrimaryKey() == null || arguments.getPrimaryKey().isEmpty())) {
            support.throwError(
                    "AgtySQL.updateAndGet()",
                    "Strategy '%s' for driver '%s' requires primary key metadata".formatted(
                            strategy, support.getConfig().getDriver())
            );
            return RowFactory.emptyRow();
        }

        if (strategy.isCollisionProne()) {
            support.debugMessage(
                    "AgtySQL.updateAndGet()",
                    "Using collision-prone follow-up strategy for driver '%s'".formatted(
                            support.getConfig().getDriver())
            );
        }

        boolean updated = update(copyArguments(arguments));
        if (!updated || support.hasErrors()) {
            return RowFactory.emptyRow();
        }

        SqlRow row = support.fetch(
                Arguments.builder()
                        .setTable(arguments.getTable())
                        .setPrimaryKey(arguments.getPrimaryKey())
                        .setWhere(arguments.getWhere())
                        .setHaving(arguments.getHaving())
                        .setGroupBy(arguments.getGroupBy())
                        .setOrderBy(arguments.getOrderBy())
                        .setFields(fields)
                        .setNoRebuildQuery(arguments.noRebuildQuery())
        );

        return row == null ? RowFactory.emptyRow() : row;
    }

    private Arguments copyArguments(Arguments source) {
        Arguments copy = Arguments.builder()
                .setTable(source.getTable())
                .setActionField(source.getActionField())
                .setPrimaryKey(source.getPrimaryKey())
                .setWhere(source.getWhere())
                .setHaving(source.getHaving())
                .setGroupBy(source.getGroupBy())
                .setOrderBy(source.getOrderBy())
                .setFields(source.getFields())
                .setQuery(source.getQuery())
                .convertValueToString(source.convertValueToString())
                .setEmulateMode(source.isEmulateMode())
                .setNoStringEncode(source.noStringEncode())
                .setNoRebuildQuery(source.noRebuildQuery())
                .setForceRebuildQuery(source.forceRequery());

        source.getDataArray().forEach((key, value) -> putData(copy, key, value));
        source.getColumns().forEach(copy::addColumn);

        if (source.hasLimit()) {
            copy.setLimit(source.getLimit());
            copy.setOffset(source.getOffset());
        }

        return copy;
    }

    private void putData(Arguments arguments, String key, Object value) {
        if (value == null) {
            arguments.setData(key, (String) null);
        } else if (value instanceof String stringValue) {
            arguments.setData(key, stringValue);
        } else if (value instanceof Integer integerValue) {
            arguments.setData(key, integerValue);
        } else if (value instanceof Long longValue) {
            arguments.setData(key, longValue);
        } else if (value instanceof Short shortValue) {
            arguments.setData(key, shortValue);
        } else if (value instanceof Boolean booleanValue) {
            arguments.setData(key, booleanValue);
        } else if (value instanceof Float floatValue) {
            arguments.setData(key, floatValue);
        } else if (value instanceof Double doubleValue) {
            arguments.setData(key, doubleValue);
        } else if (value instanceof Character characterValue) {
            arguments.setData(key, characterValue);
        } else {
            arguments.setData(key, String.valueOf(value));
        }
    }
}
