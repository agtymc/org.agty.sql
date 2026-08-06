package org.agty.sql.operations;

import org.agty.sql.AgtySqlOperationSupport;
import org.agty.sql.data.Arguments;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.model.ModelControl;
import org.agty.sql.support.RowFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class FetchOperation {

    private final AgtySqlOperationSupport support;

    public FetchOperation(AgtySqlOperationSupport support) {
        this.support = support;
    }

    public SqlRow fetch(Arguments arguments) {
        String query = support.hasQuery(arguments)
                ? arguments.getQuery()
                : support.getDriverSqlObject().fetchQuery(arguments);

        if (support.hasQuery(query)) {
            try (ResultSet resultSet = support.executeQuery(query, arguments.noRebuildQuery())) {
                return support.getFetchRow(resultSet, arguments);
            } catch (SQLException e) {
                support.throwError("AgtySQL.fetch()", e.getMessage());
            }
        } else {
            support.throwError("AgtySQL.fetch()", "No a query for FETCH");
        }

        return RowFactory.emptyRow();
    }

    public <T> T fetchEntity(Arguments arguments, T object) {
        try {
            return ModelControl.newModelControl().fetchEntity(support.getAgtySQL(), arguments, object);
        } catch (Exception e) {
            support.throwError("AgSQL.save()//[fetch(Arguments arguments, T object)]", e.getMessage());
        }
        return null;
    }

    public <T> T fetchEntity(Arguments arguments, Class<?> clazz) {
        try {
            return ModelControl.newModelControl().fetchEntity(support.getAgtySQL(), arguments, clazz);
        } catch (Exception e) {
            support.throwError("AgSQL.save()//[fetch(Arguments arguments, T object)]", e.getMessage());
        }
        return null;
    }

    public Long countRows(Arguments arguments) {
        String query = support.hasQuery(arguments)
                ? arguments.getQuery()
                : support.getDriverSqlObject().countRowsQuery(arguments);

        if (support.hasQuery(query)) {
            SqlRow getData = fetch(new Arguments().setQuery(query));
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
        return fetch(new Arguments().setQuery(query));
    }

    public SqlRow getFirstRow(Arguments arguments) {
        String query = support.getDriverSqlObject().getFirstRowQuery(arguments);
        support.debugMessage("AgtySQL.getFirstRow()", "Query: " + query);
        return fetch(new Arguments().setQuery(query));
    }
}
