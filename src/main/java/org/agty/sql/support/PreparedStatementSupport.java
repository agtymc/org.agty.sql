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
    private static final java.time.format.DateTimeFormatter SQLITE_DATE_TIME =
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
    private static final java.time.format.DateTimeFormatter SQLITE_TIME =
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSSSS");

    private PreparedStatementSupport() {
    }

    /**
     * Performs the bind operation.
     * @param statement parameter value
     * @param parameters parameter value
     * @throws SQLException if the operation cannot be completed
     */
    public static void bind(PreparedStatement statement, List<?> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            Object value = parameters.get(i);
            int parameterIndex = i + 1;

            if (value instanceof java.time.LocalDateTime dateTime) {
                if (isSqlite(statement)) {
                    bindTemporalSqlite(statement, parameterIndex, dateTime);
                } else {
                    statement.setObject(parameterIndex, dateTime);
                }
            } else if (value instanceof java.time.LocalDate date) {
                if (isSqlite(statement)) statement.setString(parameterIndex, date.toString());
                else statement.setDate(parameterIndex, java.sql.Date.valueOf(date));
            } else if (value instanceof java.time.LocalTime time) {
                if (isSqlite(statement)) bindTemporalSqlite(statement, parameterIndex, time);
                else statement.setTime(parameterIndex, java.sql.Time.valueOf(time));
            } else if (value instanceof java.util.Date date && isSqlite(statement)) {
                Object temporal = date instanceof java.sql.Date sqlDate ? sqlDate.toLocalDate()
                        : date instanceof java.sql.Time sqlTime ? sqlTime.toLocalTime()
                        : date instanceof Timestamp timestamp ? timestamp.toLocalDateTime()
                        : new Timestamp(date.getTime()).toLocalDateTime();
                // Reuse the same representation for write and WHERE parameters.
                bindTemporalSqlite(statement, parameterIndex, temporal);
            } else if (value instanceof BigDecimal decimal) {
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

    private static boolean isSqlite(PreparedStatement statement) throws SQLException {
        return "SQLite".equalsIgnoreCase(statement.getConnection().getMetaData().getDatabaseProductName());
    }

    private static void bindTemporalSqlite(PreparedStatement statement, int index, Object value) throws SQLException {
        String text = value instanceof java.time.LocalDateTime dateTime
                ? dateTime.format(SQLITE_DATE_TIME)
                : value instanceof java.time.LocalTime time
                ? time.format(SQLITE_TIME) : value.toString();
        statement.setString(index, text);
    }

    /**
     * Performs the execute query operation.
     * @param sql parameter value
     * @param query parameter value
     * @param parameters parameter value
     * @param noRebuildQuery parameter value
     * @param errorType parameter value
     * @return operation result
     */
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

    /**
     * Performs the read parameters operation.
     * @param arguments parameter value
     * @return operation result
     */
    public static List<Object> readParameters(Arguments arguments) {
        return arguments.hasQuery()
                ? arguments.getQueryParameters()
                : arguments.getWhereParameters();
    }

    /**
     * Performs the read query arguments operation.
     * @param source parameter value
     * @param query parameter value
     * @return operation result
     */
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

    /**
     * Performs the insert parameters operation.
     * @param arguments parameter value
     * @return operation result
     */
    public static List<Object> insertParameters(Arguments arguments) {
        return arguments.hasQuery()
                ? arguments.getQueryParameters()
                : arguments.getDataValues();
    }

    /**
     * Performs the insert parameters operation.
     * @param arguments parameter value
     * @return operation result
     */
    public static List<Object> insertParameters(List<Arguments> arguments) {
        List<Object> parameters = new ArrayList<>();
        for (Arguments item : arguments) {
            parameters.addAll(item.getDataValues());
        }
        return parameters;
    }

    /**
     * Performs the update parameters operation.
     * @param arguments parameter value
     * @return operation result
     */
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
