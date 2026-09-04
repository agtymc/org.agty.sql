package org.agty.sql.support;

import org.agty.sql.AgtySQL;
import org.agty.sql.data.Arguments;
import org.agty.sql.data.SqlExpression;
import org.agty.sql.exceptions.AgtySqlException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Internal helpers for the opt-in high-level PreparedStatement mode.
 */
public final class PreparedStatementSupport {

    private PreparedStatementSupport() {
    }

    public static void bind(PreparedStatement statement, List<?> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            Object value = parameters.get(i);
            int parameterIndex = i + 1;

            if (value instanceof BigDecimal decimal) {
                statement.setBigDecimal(parameterIndex, decimal);
            } else if (value instanceof BigInteger integer) {
                statement.setBigDecimal(parameterIndex, new BigDecimal(integer));
            } else if (value instanceof String stringValue) {
                statement.setString(parameterIndex, stringValue);
            } else if (value instanceof Byte byteValue) {
                statement.setByte(parameterIndex, byteValue);
            } else if (value instanceof Character character) {
                statement.setString(parameterIndex, character.toString());
            } else if (value instanceof Enum<?> enumValue) {
                statement.setString(parameterIndex, enumValue.name());
            } else if (value instanceof java.util.Date date
                    && !(value instanceof java.sql.Date)
                    && !(value instanceof java.sql.Time)
                    && !(value instanceof java.sql.Timestamp)) {
                statement.setTimestamp(parameterIndex, new Timestamp(date.getTime()));
            } else {
                statement.setObject(parameterIndex, value);
            }
        }
    }

    public static ResultSet executeQuery(
            AgtySQL sql,
            String query,
            List<?> parameters,
            boolean noRebuildQuery,
            String errorType
    ) {
        PreparedStatement statement = sql.prepareStatement(query, noRebuildQuery);
        if (statement == null) {
            return null;
        }

        try {
            bind(statement, parameters);
            return statement.executeQuery();
        } catch (SQLException e) {
            closeAfterFailure(statement, e);
            sql.setAndLogError(errorType, e.getMessage());
            if (sql.getConfig().isThrowException()) {
                throw new AgtySqlException(errorType, e.getMessage(), e);
            }
            return null;
        }
    }

    public static List<Object> readParameters(Arguments arguments) {
        return arguments.hasQuery()
                ? arguments.getQueryParameters()
                : arguments.getWhereParameters();
    }

    public static Arguments readQueryArguments(Arguments source, String query) {
        Arguments target = Arguments.builder()
                .useStatementPrepare(source.useStatementPrepare())
                .convertValueToString(source.convertValueToString())
                .setNoRebuildQuery(source.noRebuildQuery())
                .setForceRebuildQuery(source.forceRebuildQuery());

        if (source.useStatementPrepare()) {
            target.setQuery(query, readParameters(source).toArray());
        } else {
            target.setQuery(SqlExpression.trusted(query));
        }

        return target;
    }

    public static List<Object> insertParameters(Arguments arguments) {
        return arguments.hasQuery()
                ? arguments.getQueryParameters()
                : arguments.getDataValues();
    }

    public static List<Object> insertParameters(List<Arguments> arguments) {
        List<Object> parameters = new ArrayList<>();
        for (Arguments item : arguments) {
            parameters.addAll(item.getDataValues());
        }
        return parameters;
    }

    public static List<Object> updateParameters(Arguments arguments) {
        if (arguments.hasQuery()) {
            return arguments.getQueryParameters();
        }

        List<Object> parameters = new ArrayList<>(arguments.getDataValues());
        parameters.addAll(arguments.getWhereParameters());
        return parameters;
    }

    private static void closeAfterFailure(PreparedStatement statement, SQLException failure) {
        try {
            statement.close();
        } catch (SQLException closeException) {
            failure.addSuppressed(closeException);
        }
    }
}
