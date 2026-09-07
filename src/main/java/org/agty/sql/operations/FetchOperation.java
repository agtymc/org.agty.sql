package org.agty.sql.operations;

import org.agty.sql.AgtySqlOperationSupport;
import org.agty.sql.data.Arguments;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.model.ModelControl;
import org.agty.sql.support.PreparedStatementSupport;
import org.agty.sql.support.RowFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Provides fetch operation behavior.
 */
public final class FetchOperation {

    private final AgtySqlOperationSupport support;

    /**
     * Creates a new instance.
     * @param support parameter value
     */
    public FetchOperation(AgtySqlOperationSupport support) {
        this.support = support;
    }

    /**
     * Performs the fetch operation.
     * @param arguments parameter value
     * @return operation result
     */
    public SqlRow fetch(Arguments arguments) {
        String query = support.hasQuery(arguments)
                ? arguments.getQuery()
                : support.getDriverSqlObject().fetchQuery(arguments);

        return fetchQuery(query, arguments, "AgtySQL.fetch()");
    }

    private SqlRow fetchQuery(String query, Arguments arguments, String errorType) {
        if (support.hasQuery(query)) {
            ResultSet resultSet = executeQuery(query, arguments);
            try (Statement statement = resultSet == null ? null : resultSet.getStatement();
                 ResultSet closeableResultSet = resultSet) {
                return support.getFetchRow(closeableResultSet, arguments);
            } catch (SQLException e) {
                support.throwError(errorType, e);
            }
        } else {
            support.throwError(errorType, "No a query for FETCH");
        }

        return RowFactory.emptyRow();
    }

    private ResultSet executeQuery(String query, Arguments arguments) {
        if (!arguments.useStatementPrepare()) {
            return support.executeQuery(query, arguments.noRebuildQuery());
        }

        return support.executePreparedQuery(
                query,
                PreparedStatementSupport.readParameters(arguments),
                arguments.noRebuildQuery()
        );
    }

    /**
     * Performs the fetch entity operation.
     * @param <T> type parameter
     * @param arguments parameter value
     * @param object parameter value
     * @return operation result
     */
    public <T> T fetchEntity(Arguments arguments, T object) {
        try {
            return ModelControl.newModelControl().fetchEntity(support.getAgtySQL(), arguments, object);
        } catch (Exception e) {
            support.throwError("AgSQL.save()//[fetch(Arguments arguments, T object)]", e);
        }
        return null;
    }

    /**
     * Performs the fetch entity operation.
     * @param <T> type parameter
     * @param arguments parameter value
     * @param clazz parameter value
     * @return operation result
     */
    public <T> T fetchEntity(Arguments arguments, Class<?> clazz) {
        try {
            return ModelControl.newModelControl().fetchEntity(support.getAgtySQL(), arguments, clazz);
        } catch (Exception e) {
            support.throwError("AgSQL.save()//[fetch(Arguments arguments, T object)]", e);
        }
        return null;
    }

    /**
     * Performs the count rows operation.
     * @param arguments parameter value
     * @return operation result
     */
    public Long countRows(Arguments arguments) {
        String query = support.hasQuery(arguments)
                ? arguments.getQuery()
                : support.getDriverSqlObject().countRowsQuery(arguments);

        if (support.hasQuery(query)) {
            SqlRow getData = fetchQuery(query, arguments, "AgtySQL.countRows()");
            return getData.getLong("rows");
        }

        support.throwError("AgtySQL.countRows()", "No query for countRows");
        return 0L;
    }

    /**
     * Performs the table is exists operation.
     * @param arguments parameter value
     * @return operation result
     */
    public boolean tableIsExists(Arguments arguments) {
        return support.getDriverSqlObject().tableIsExists(support.rebuildTable(arguments.getTable()));
    }

    /**
     * Performs the row is exists operation.
     * @param arguments parameter value
     * @return operation result
     */
    public Boolean rowIsExists(Arguments arguments) {
        return support.getDriverSqlObject().rowIsExists(arguments);
    }

    /**
     * Returns the last row.
     * @param arguments parameter value
     * @return operation result
     */
    public SqlRow getLastRow(Arguments arguments) {
        String query = support.getDriverSqlObject().getLastRowQuery(arguments);
        support.debugMessage("AgtySQL.getLastRow()", "Query: " + query);
        return fetchQuery(query, arguments, "AgtySQL.getLastRow()");
    }

    /**
     * Returns the first row.
     * @param arguments parameter value
     * @return operation result
     */
    public SqlRow getFirstRow(Arguments arguments) {
        String query = support.getDriverSqlObject().getFirstRowQuery(arguments);
        support.debugMessage("AgtySQL.getFirstRow()", "Query: " + query);
        return fetchQuery(query, arguments, "AgtySQL.getFirstRow()");
    }
}
