package org.agty.sql.operations;

import org.agty.sql.AgtySqlOperationSupport;
import org.agty.sql.data.Arguments;
import org.agty.sql.data.SqlExpression;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.support.PreparedStatementSupport;
import org.agty.sql.support.RowFactory;
import org.agty.sql.support.SqlIdentifierValidator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

public final class InsertOperation {

    private final AgtySqlOperationSupport support;

    public InsertOperation(AgtySqlOperationSupport support) {
        this.support = support;
    }

    public long insert(Arguments arguments) {
        if (arguments.returnLastInsertId() && !support.getDialectCapabilities().supportsLastInsertId()) {
            support.throwError(
                    "AgtySQL.insert()",
                    "Driver '%s' does not support lastInsertId()".formatted(support.getConfig().getDriver())
            );
            return 0L;
        }

        String query = support.hasQuery(arguments)
                ? arguments.getQuery()
                : support.getDriverSqlObject().insertQuery(arguments);

        if (arguments.returnLastInsertId()) {
            return insertAndReturnLastInsertId(arguments, query);
        } else if (arguments.useStatementPrepare()) {
            support.executePreparedUpdate(
                    query,
                    PreparedStatementSupport.insertParameters(arguments),
                    arguments.noRebuildQuery()
            );
        } else {
            support.execute(query, arguments.noRebuildQuery());
        }

        return 0L;
    }

    private long insertAndReturnLastInsertId(Arguments arguments, String query) {
        Long explicitPrimaryKeyValue = resolveExplicitPrimaryKeyValue(arguments);

        try (PreparedStatement preparedStatement = support.prepareStatement(
                query,
                Statement.RETURN_GENERATED_KEYS,
                arguments.noRebuildQuery()
        )) {
            if (preparedStatement == null) {
                return 0L;
            }

            if (arguments.useStatementPrepare()) {
                support.bind(
                        preparedStatement,
                        PreparedStatementSupport.insertParameters(arguments)
                );
            }
            preparedStatement.executeUpdate();

            if (explicitPrimaryKeyValue != null) {
                return explicitPrimaryKeyValue;
            }

            Long generatedKey = extractGeneratedKey(
                    support.getGeneratedKeys(preparedStatement, arguments.convertValueToString()),
                    arguments
            );
            if (generatedKey != null) {
                return generatedKey;
            }

            Long fallbackLastInsertId = support.lastInsertId(arguments);
            return fallbackLastInsertId == null ? 0L : fallbackLastInsertId;
        } catch (SQLException e) {
            support.throwError("AgtySQL.insert()", e);
            return 0L;
        }
    }

    private Long resolveExplicitPrimaryKeyValue(Arguments arguments) {
        String primaryKey = resolvePrimaryKeyName(arguments);
        if (primaryKey == null || primaryKey.isEmpty()) {
            return null;
        }

        Object primaryKeyValue = findDataValue(arguments, primaryKey);
        return toLongValue(primaryKeyValue);
    }

    private Long extractGeneratedKey(SqlRow generatedKeys, Arguments arguments) {
        if (generatedKeys == null || generatedKeys.isEmpty()) {
            return null;
        }

        String primaryKey = resolvePrimaryKeyName(arguments);
        if (primaryKey != null) {
            Long primaryKeyValue = generatedKeys.getLong(primaryKey);
            if (primaryKeyValue != null) {
                return primaryKeyValue;
            }
        }

        for (String candidateKey : new String[]{"GENERATED_KEY", "generated_key", "ID", "id", "last_id", "lastId"}) {
            Long generatedValue = generatedKeys.getLong(candidateKey);
            if (generatedValue != null) {
                return generatedValue;
            }
        }

        if (generatedKeys instanceof Map<?, ?> generatedKeysMap) {
            for (Object value : generatedKeysMap.values()) {
                Long generatedValue = toLongValue(value);
                if (generatedValue != null) {
                    return generatedValue;
                }
            }
        }

        return null;
    }

    private String resolvePrimaryKeyName(Arguments arguments) {
        if (arguments.getPrimaryKey() != null && !arguments.getPrimaryKey().isEmpty()) {
            return arguments.getPrimaryKey();
        }

        if (!arguments.hasTable()) {
            return null;
        }

        return support.getDriverSqlObject().getPrimaryKey(arguments);
    }

    private Object findDataValue(Arguments arguments, String key) {
        Object exactValue = arguments.getData(key);
        if (exactValue != null) {
            return exactValue;
        }

        for (Map.Entry<String, Object> entry : arguments.getDataMap().entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }

        return null;
    }

    private Long toLongValue(Object value) {
        if (value instanceof Number numberValue) {
            return numberValue.longValue();
        }

        if (value instanceof CharSequence textValue) {
            try {
                return Long.parseLong(textValue.toString().trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    public SqlRow insertAndGet(Arguments arguments, String fields) {
        return insertAndGetValidated(arguments, SqlIdentifierValidator.requireFieldList(fields));
    }

    public SqlRow insertAndGet(Arguments arguments, SqlExpression fields) {
        return insertAndGetValidated(arguments, requireExpression(fields, "RETURNING fields"));
    }

    private SqlRow insertAndGetValidated(Arguments arguments, String fields) {
        if (!support.getDialectCapabilities().supportsInsertAndGet()) {
            support.throwError(
                    "AgtySQL.insertAndGet()",
                    "Driver '%s' does not support insertAndGet()".formatted(support.getConfig().getDriver())
            );
            return RowFactory.emptyRow();
        }

        if (support.getDialectCapabilities().supportsInsertAndGetReturning()) {
            return insertAndGetReturning(arguments, fields);
        }

        if (support.getDialectCapabilities().usesFollowUpFetchForInsertAndGet()) {
            return insertAndGetFollowUpFetch(arguments, fields);
        }

        support.throwError(
                "AgtySQL.insertAndGet()",
                "Driver '%s' does not have a documented insertAndGet() strategy".formatted(
                        support.getConfig().getDriver()
                )
        );
        return RowFactory.emptyRow();
    }

    private SqlRow insertAndGetReturning(Arguments arguments, String fields) {
        ResultSet resultSet = support.getDriverSqlObject().insertAndGet(arguments, fields);
        try (Statement statement = resultSet == null ? null : resultSet.getStatement();
             ResultSet closeableResultSet = resultSet) {
            return support.getFetchRow(closeableResultSet, arguments);
        } catch (SQLException e) {
            support.throwError("AgtySQL.insertAndGet()", e);
        }

        return RowFactory.emptyRow();
    }

    private SqlRow insertAndGetFollowUpFetch(Arguments arguments, String fields) {
        if (!arguments.hasTable()) {
            support.throwError(
                    "AgtySQL.insertAndGet()",
                    "Follow-up insertAndGet() requires table metadata for driver '%s'".formatted(
                            support.getConfig().getDriver()
                    )
            );
            return RowFactory.emptyRow();
        }

        String primaryKey = support.getDriverSqlObject().getPrimaryKey(arguments);

        if (primaryKey == null || primaryKey.isEmpty()) {
            support.throwError(
                    "AgtySQL.insertAndGet()",
                    "Primary key is required for follow-up insertAndGet() strategy"
            );
            return RowFactory.emptyRow();
        }

        Arguments insertArguments = copyArguments(arguments)
                .setReturnLastInsertId(true)
                .setPrimaryKey(primaryKey);

        long insertedId = insert(insertArguments);
        if (support.hasErrors()) {
            return RowFactory.emptyRow();
        }

        if (support.getDialectCapabilities().usesUnsafeLastInsertIdFallback()) {
            support.debugMessage(
                    "AgtySQL.insertAndGet()",
                    "Using collision-prone follow-up fetch for driver '%s'".formatted(
                            support.getConfig().getDriver()
                    )
            );
        }

        Arguments fetchArguments = Arguments.builder()
                .useStatementPrepare(arguments.useStatementPrepare())
                .setTable(arguments.getTable())
                .setPrimaryKey(primaryKey)
                .setFields(SqlExpression.trusted(fields));

        if (arguments.useStatementPrepare()) {
            fetchArguments.setWhere("[%s] = ?".formatted(primaryKey), insertedId);
        } else {
            fetchArguments.setWhere("[" + primaryKey + "] = %d", insertedId);
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

        copyStructuralArguments(source, copy);

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

    private void copyStructuralArguments(Arguments source, Arguments target) {
        if (source.hasActionField()) {
            target.setActionField(source.getActionField());
        }
        if (source.getPrimaryKey() != null && !source.getPrimaryKey().isBlank()) {
            target.setPrimaryKey(source.getPrimaryKey());
        }
        if (source.hasHaving()) {
            target.setHaving(SqlExpression.trusted(source.getHaving()));
        }
        if (source.hasGroupBy()) {
            target.setGroupBy(SqlExpression.trusted(source.getGroupBy()));
        }
        if (source.hasOrderBy()) {
            target.setOrderBy(SqlExpression.trusted(source.getOrderBy()));
        }
        target.setFields(SqlExpression.trusted(source.getFields()));
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

    public void insert(ArrayList<Arguments> arguments) {
        String query = support.getDriverSqlObject().insertQuery(arguments);
        boolean prepared = arguments.stream().anyMatch(Arguments::useStatementPrepare);

        if (prepared && arguments.stream().anyMatch(item -> !item.useStatementPrepare())) {
            support.throwError(
                    "AgtySQL.insert()",
                    "All rows in a prepared multi-row insert must enable useStatementPrepare(true)"
            );
            return;
        }

        if (prepared) {
            support.executePreparedUpdate(
                    query,
                    PreparedStatementSupport.insertParameters(arguments),
                    false
            );
        } else {
            support.execute(query, false);
        }
    }
}
