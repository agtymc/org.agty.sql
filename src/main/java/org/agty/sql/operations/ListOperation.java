package org.agty.sql.operations;

import org.agty.sql.AgtySqlCursor;
import org.agty.sql.AgtySqlOperationSupport;
import org.agty.sql.data.Arguments;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.support.RowFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;

public final class ListOperation {

    private final AgtySqlOperationSupport support;

    public ListOperation(AgtySqlOperationSupport support) {
        this.support = support;
    }

    public SqlRow list(Arguments arguments, int index) {
        if (!support.hasListResultSet(index)) {
            createListResultSet(arguments, index);
        }

        if (!support.hasListResultSet(index)) {
            support.debugMessage("list(%d)".formatted(index), "The ResultSet has not been created");
            return null;
        }

        return getListData(index, arguments);
    }

    public AgtySqlCursor openCursor(Arguments arguments) {
        String query = support.hasQuery(arguments)
                ? arguments.getQuery()
                : support.getDriverSqlObject().selectQuery(arguments);

        if (!support.hasQuery(query)) {
            support.throwError("AgtySQL.openCursor()", "A query is empty");
            return null;
        }

        support.debugMessage("AgtySQL.openCursor()", "QUERY: " + query);
        return new AgtySqlCursor(
                support.executeQuery(query, arguments.noRebuildQuery()),
                arguments
        );
    }

    public LinkedList<SqlRow> listArray(Arguments arguments) {
        LinkedList<SqlRow> list = new LinkedList<>();
        ResultSet resultSet = null;

        String query = support.hasQuery(arguments)
                ? arguments.getQuery()
                : support.getDriverSqlObject().selectQuery(arguments);

        try {
            if (support.hasQuery(query)) {
                support.debugMessage("createListResultSet()", "QUERY: " + query);
                resultSet = support.executeQuery(query, arguments.noRebuildQuery());
            } else {
                support.throwError("AgtySQL.createListResultSet()", "A query is empty");
                return null;
            }

            while (resultSet.next()) {
                SqlRow row = RowFactory.newSqlRow();
                ResultSetMetaData md = resultSet.getMetaData();

                for (int i = 1; i <= md.getColumnCount(); ++i) {
                    row.setData(
                            md.getColumnName(i),
                            arguments.convertValueToString() ? resultSet.getString(i) : resultSet.getObject(i)
                    );
                }

                list.add(row);
            }
        } catch (SQLException e) {
            support.throwError("AgSQL.getList()", e.getMessage());
        } finally {
            closeResultSet(resultSet);
        }

        return list;
    }

    private void createListResultSet(Arguments arguments, int index) {
        support.debugMessageEnterInMethod("createListResultSet(%d)".formatted(index));

        String query = support.hasQuery(arguments)
                ? arguments.getQuery()
                : support.getDriverSqlObject().selectQuery(arguments);

        if (support.hasQuery(query)) {
            support.debugMessage("createListResultSet(%d)".formatted(index), "QUERY: " + query);
            support.setListResultSet(
                    support.executeQuery(query, arguments.noRebuildQuery()),
                    index
            );
        } else {
            support.throwError("AgtySQL.createListResultSet(%d)".formatted(index), "A query is empty");
        }
    }

    private SqlRow getListData(int index, Arguments arguments) {
        ResultSet resultSet = support.getListResultSet(index);

        try {
            if (resultSet != null && resultSet.next()) {
                SqlRow rowData = RowFactory.newSqlRow();
                ResultSetMetaData md = resultSet.getMetaData();

                for (int i = 1; i <= md.getColumnCount(); ++i) {
                    rowData.setData(
                            md.getColumnName(i),
                            arguments.convertValueToString() ? resultSet.getString(i) : resultSet.getObject(i)
                    );
                }

                return rowData;
            }
        } catch (SQLException e) {
            support.throwError("AgSQL.getList(%d)".formatted(index), e.getMessage());
        }

        support.clearListResultSet(index);
        return null;
    }

    private void closeResultSet(ResultSet resultSet) {
        if (resultSet == null) {
            return;
        }

        java.sql.Statement statement = null;
        SQLException exception = null;

        try {
            statement = resultSet.getStatement();
        } catch (SQLException e) {
            exception = e;
        }

        try {
            if (!resultSet.isClosed()) {
                resultSet.close();
            }
        } catch (SQLException e) {
            exception = e;
        }

        try {
            if (statement != null && !statement.isClosed()) {
                statement.close();
            }
        } catch (SQLException e) {
            if (exception == null) {
                exception = e;
            }
        }

        if (exception != null) {
            support.throwError("AgSQL.closeResultSet()", exception.getMessage());
        }
    }
}
