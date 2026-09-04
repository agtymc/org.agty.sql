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

public final class FetchOperation {

    private final AgtySqlOperationSupport support;

    public FetchOperation(AgtySqlOperationSupport support) {
        this.support = support;
    }

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

    public <T> T fetchEntity(Arguments arguments, T object) {
        try {
            return ModelControl.newModelControl().fetchEntity(support.getAgtySQL(), arguments, object);
        } catch (Exception e) {
            support.throwError("AgSQL.save()//[fetch(Arguments arguments, T object)]", e);
        }
        return null;
    }

    public <T> T fetchEntity(Arguments arguments, Class<?> clazz) {
        try {
            return ModelControl.newModelControl().fetchEntity(support.getAgtySQL(), arguments, clazz);
        } catch (Exception e) {
            support.throwError("AgSQL.save()//[fetch(Arguments arguments, T object)]", e);
        }
        return null;
    }

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

    public boolean tableIsExists(Arguments arguments) {
        return support.getDriverSqlObject().tableIsExists(support.rebuildTable(arguments.getTable()));
    }

    public Boolean rowIsExists(Arguments arguments) {
        return support.getDriverSqlObject().rowIsExists(arguments);
    }

    public SqlRow getLastRow(Arguments arguments) {
        String query = support.getDriverSqlObject().getLastRowQuery(arguments);
        support.debugMessage("AgtySQL.getLastRow()", "Query: " + query);
        return fetchQuery(query, arguments, "AgtySQL.getLastRow()");
    }

    public SqlRow getFirstRow(Arguments arguments) {
        String query = support.getDriverSqlObject().getFirstRowQuery(arguments);
        support.debugMessage("AgtySQL.getFirstRow()", "Query: " + query);
        return fetchQuery(query, arguments, "AgtySQL.getFirstRow()");
    }
}
