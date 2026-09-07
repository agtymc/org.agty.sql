package org.agty.sql;

import org.agty.sql.config.AgtySqlConfig;
import org.agty.sql.data.Arguments;
import org.agty.sql.driver.DialectCapabilities;
import org.agty.sql.interfaces.Sql;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.support.PreparedStatementSupport;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Internal bridge used by operation classes moved out of the root package.
 */
public final class AgtySqlOperationSupport {

    private final AgtySQL agtySQL;

    AgtySqlOperationSupport(AgtySQL agtySQL) {
        this.agtySQL = agtySQL;
    }

    /**
     * Returns the config.
     * @return operation result
     */
    public AgtySqlConfig getConfig() {
        return agtySQL.getConfig();
    }

    /**
     * Returns the agty sql.
     * @return operation result
     */
    public AgtySQL getAgtySQL() {
        return agtySQL;
    }

    /**
     * Returns whether errors.
     * @return operation result
     */
    public boolean hasErrors() {
        return agtySQL.hasErrors();
    }

    /**
     * Returns whether query.
     * @param arguments parameter value
     * @return operation result
     */
    public boolean hasQuery(Arguments arguments) {
        return agtySQL.hasQueryInternal(arguments);
    }

    /**
     * Returns whether query.
     * @param query parameter value
     * @return operation result
     */
    public boolean hasQuery(String query) {
        return agtySQL.hasQueryInternal(query);
    }

    /**
     * Returns the driver sql object.
     * @return operation result
     */
    public Sql getDriverSqlObject() {
        return agtySQL.getDriverSqlObjectInternal();
    }

    /**
     * Returns the fetch row.
     * @param resultSet parameter value
     * @param arguments parameter value
     * @return operation result
     * @throws SQLException if the operation cannot be completed
     */
    public SqlRow getFetchRow(ResultSet resultSet, Arguments arguments) throws SQLException {
        return agtySQL.getFetchRowInternal(resultSet, arguments);
    }

    /**
     * Performs the execute query operation.
     * @param query parameter value
     * @param noRebuildQuery parameter value
     * @return operation result
     */
    public ResultSet executeQuery(String query, boolean noRebuildQuery) {
        return agtySQL.executeQueryInternal(query, noRebuildQuery);
    }

    /**
     * Performs the create managed cursor operation.
     * @param resultSet parameter value
     * @param arguments parameter value
     * @return operation result
     */
    public AgtySqlCursor createManagedCursor(ResultSet resultSet, Arguments arguments) {
        return agtySQL.createManagedCursor(resultSet, arguments);
    }

    /**
     * Performs the execute operation.
     * @param query parameter value
     * @param noRebuildQuery parameter value
     * @return operation result
     */
    public boolean execute(String query, boolean noRebuildQuery) {
        return agtySQL.execute(query, noRebuildQuery);
    }

    /**
     * Performs the execute update operation.
     * @param query parameter value
     * @return operation result
     */
    public Long executeUpdate(String query) {
        return agtySQL.executeUpdate(query);
    }

    /**
     * Performs the execute prepared query operation.
     * @param query parameter value
     * @param parameters parameter value
     * @param noRebuildQuery parameter value
     * @return operation result
     */
    public ResultSet executePreparedQuery(
            String query,
            List<?> parameters,
            boolean noRebuildQuery
    ) {
        PreparedStatement statement = agtySQL.prepareStatement(query, noRebuildQuery);
        if (statement == null) {
            return null;
        }

        try {
            PreparedStatementSupport.bind(statement, parameters);
            return statement.executeQuery();
        } catch (SQLException e) {
            closeAfterFailure(statement, e);
            agtySQL.throwErrorInternal("AgtySQL.executePreparedQuery()", e);
            return null;
        }
    }

    /**
     * Performs the execute prepared update operation.
     * @param query parameter value
     * @param parameters parameter value
     * @param noRebuildQuery parameter value
     * @return operation result
     */
    public long executePreparedUpdate(
            String query,
            List<?> parameters,
            boolean noRebuildQuery
    ) {
        try (PreparedStatement statement = agtySQL.prepareStatement(query, noRebuildQuery)) {
            if (statement == null) {
                return 0L;
            }

            PreparedStatementSupport.bind(statement, parameters);
            return getDriverSqlObject().isSupportLargeUpdate()
                    ? statement.executeLargeUpdate()
                    : statement.executeUpdate();
        } catch (SQLException e) {
            agtySQL.throwErrorInternal("AgtySQL.executePreparedUpdate()", e);
            return 0L;
        }
    }

    /**
     * Performs the last insert id operation.
     * @param arguments parameter value
     * @return operation result
     */
    public Long lastInsertId(Arguments arguments) {
        return agtySQL.lastInsertIdInternal(arguments);
    }

    /**
     * Performs the prepare statement operation.
     * @param query parameter value
     * @param autoGeneratedKeys parameter value
     * @param noRebuildQuery parameter value
     * @return operation result
     */
    public PreparedStatement prepareStatement(String query, int autoGeneratedKeys, boolean noRebuildQuery) {
        return agtySQL.prepareStatement(query, autoGeneratedKeys, noRebuildQuery);
    }

    /**
     * Performs the prepare statement operation.
     * @param query parameter value
     * @param noRebuildQuery parameter value
     * @return operation result
     */
    public PreparedStatement prepareStatement(String query, boolean noRebuildQuery) {
        return agtySQL.prepareStatement(query, noRebuildQuery);
    }

    /**
     * Performs the bind operation.
     * @param statement parameter value
     * @param parameters parameter value
     * @throws SQLException if the operation cannot be completed
     */
    public void bind(PreparedStatement statement, List<?> parameters) throws SQLException {
        PreparedStatementSupport.bind(statement, parameters);
    }

    /**
     * Performs the rebuild table operation.
     * @param table parameter value
     * @return operation result
     */
    public String rebuildTable(String table) {
        return agtySQL.rebuildTable(table);
    }

    /**
     * Performs the fetch operation.
     * @param arguments parameter value
     * @return operation result
     */
    public SqlRow fetch(Arguments arguments) {
        return agtySQL.fetch(arguments);
    }

    /**
     * Performs the fetch operation.
     * @param query parameter value
     * @return operation result
     */
    public SqlRow fetch(String query) {
        return agtySQL.fetch(query);
    }

    /**
     * Performs the throw error operation.
     * @param type parameter value
     * @param message parameter value
     */
    public void throwError(String type, String message) {
        agtySQL.throwErrorInternal(type, message);
    }

    /**
     * Performs the throw error operation.
     * @param type parameter value
     * @param cause parameter value
     */
    public void throwError(String type, Throwable cause) {
        agtySQL.throwErrorInternal(type, cause);
    }

    /**
     * Performs the debug message operation.
     * @param type parameter value
     * @param message parameter value
     */
    public void debugMessage(String type, String message) {
        agtySQL.debugMessageInternal(type, message);
    }

    /**
     * Performs the debug message enter in method operation.
     * @param method parameter value
     */
    public void debugMessageEnterInMethod(String method) {
        agtySQL.debugMessageEnterInMethodInternal(method);
    }

    /**
     * Returns the list result set.
     * @param index parameter value
     * @return operation result
     */
    public ResultSet getListResultSet(int index) {
        return agtySQL.getListResultSetInternal(index);
    }

    /**
     * Sets the list result set.
     * @param resultSet parameter value
     * @param index parameter value
     */
    public void setListResultSet(ResultSet resultSet, int index) {
        agtySQL.setListResultSetInternal(resultSet, index);
    }

    /**
     * Returns whether list result set.
     * @param index parameter value
     * @return operation result
     */
    public boolean hasListResultSet(int index) {
        return agtySQL.hasListResultSetInternal(index);
    }

    /**
     * Performs the clear list result set operation.
     * @param index parameter value
     */
    public void clearListResultSet(int index) {
        agtySQL.clearListResultSetInternal(index);
    }

    /**
     * Returns the dialect capabilities.
     * @return operation result
     */
    public DialectCapabilities getDialectCapabilities() {
        return agtySQL.getDialectCapabilities();
    }

    /**
     * Returns the generated keys.
     * @param preparedStatement parameter value
     * @param convertValueToString parameter value
     * @return operation result
     */
    public SqlRow getGeneratedKeys(PreparedStatement preparedStatement, boolean convertValueToString) {
        return agtySQL.getGeneratedKeys(preparedStatement, convertValueToString);
    }

    private void closeAfterFailure(PreparedStatement statement, SQLException failure) {
        try {
            statement.close();
        } catch (SQLException closeException) {
            failure.addSuppressed(closeException);
        }
    }
}
