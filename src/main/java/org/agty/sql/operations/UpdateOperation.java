package org.agty.sql.operations;

import org.agty.sql.AgtySqlOperationSupport;
import org.agty.sql.data.Arguments;
import org.agty.sql.data.SqlExpression;
import org.agty.sql.driver.UpdateAndGetStrategy;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.support.PreparedStatementSupport;
import org.agty.sql.support.RowFactory;
import org.agty.sql.support.SqlIdentifierValidator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
            if (arguments.useStatementPrepare()) {
                support.executePreparedUpdate(
                        query,
                        PreparedStatementSupport.updateParameters(arguments),
                        arguments.noRebuildQuery()
                );
            } else {
                support.executeUpdate(query);
            }
        } else {
            support.throwError("AgtySQL.update()", "No a query for UPDATE");
        }

        return !support.hasErrors();
    }

    public SqlRow updateAndGet(Arguments arguments, String fields) {
        return updateAndGetValidated(arguments, SqlIdentifierValidator.requireFieldList(fields));
    }

    public SqlRow updateAndGet(Arguments arguments, SqlExpression fields) {
        return updateAndGetValidated(arguments, requireExpression(fields, "RETURNING fields"));
    }

    private SqlRow updateAndGetValidated(Arguments arguments, String fields) {
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
        try (Statement statement = resultSet == null ? null : resultSet.getStatement();
             ResultSet closeableResultSet = resultSet) {
            return support.getFetchRow(closeableResultSet, arguments);
        } catch (SQLException e) {
            support.throwError("AgtySQL.updateAndGet()", e);
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

        Arguments fetchArguments = Arguments.builder()
                .useStatementPrepare(arguments.useStatementPrepare())
                .setTable(arguments.getTable())
                .setFields(SqlExpression.trusted(fields))
                .setNoRebuildQuery(arguments.noRebuildQuery());

        copyOptionalIdentifiers(arguments, fetchArguments);
        copyQueryClauses(arguments, fetchArguments);

        if (arguments.useStatementPrepare()) {
            fetchArguments.setWhere(arguments.getWhere(), arguments.getWhereParameters().toArray());
        } else {
            fetchArguments.setWhere(SqlExpression.trusted(arguments.getWhere()));
        }

        SqlRow row = support.fetch(fetchArguments);

        return row == null ? RowFactory.emptyRow() : row;
    }

    private Arguments copyArguments(Arguments source) {
        Arguments copy = Arguments.builder()
                .useStatementPrepare(source.useStatementPrepare())
                .setTable(source.getTable())
                .convertValueToString(source.convertValueToString())
                .setEmulateMode(source.isEmulateMode())
                .setNoStringEncode(source.noStringEncode())
                .setNoRebuildQuery(source.noRebuildQuery())
                .setForceRebuildQuery(source.forceRebuildQuery());

        copyOptionalIdentifiers(source, copy);
        copyQueryClauses(source, copy);
        copy.setFields(SqlExpression.trusted(source.getFields()));

        if (source.hasWhere() && source.useStatementPrepare()) {
            copy.setWhere(source.getWhere(), source.getWhereParameters().toArray());
        } else if (source.hasWhere()) {
            copy.setWhere(SqlExpression.trusted(source.getWhere()));
        }
        if (source.hasQuery() && source.useStatementPrepare()) {
            copy.setQuery(source.getQuery(), source.getQueryParameters().toArray());
        } else if (source.hasQuery()) {
            copy.setQuery(SqlExpression.trusted(source.getQuery()));
        }

        source.getDataMap().forEach((key, value) -> putData(copy, key, value));
        source.getColumns().forEach(copy::addColumn);

        if (source.hasLimit()) {
            copy.setLimit(source.getLimit());
            copy.setOffset(source.getOffset());
        }

        return copy;
    }

    private void copyOptionalIdentifiers(Arguments source, Arguments target) {
        if (source.hasActionField()) {
            target.setActionField(source.getActionField());
        }
        if (source.getPrimaryKey() != null && !source.getPrimaryKey().isBlank()) {
            target.setPrimaryKey(source.getPrimaryKey());
        }
    }

    private void copyQueryClauses(Arguments source, Arguments target) {
        if (source.hasHaving()) {
            target.setHaving(SqlExpression.trusted(source.getHaving()));
        }
        if (source.hasGroupBy()) {
            target.setGroupBy(SqlExpression.trusted(source.getGroupBy()));
        }
        if (source.hasOrderBy()) {
            target.setOrderBy(SqlExpression.trusted(source.getOrderBy()));
        }
    }

    private String requireExpression(SqlExpression expression, String role) {
        if (expression == null) {
            throw new IllegalArgumentException(role + " expression must not be null");
        }
        return expression.sql();
    }

    private void putData(Arguments arguments, String key, Object value) {
        arguments.addData(key, value);
    }
}
