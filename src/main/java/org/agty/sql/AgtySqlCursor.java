package org.agty.sql;

import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.data.Arguments;
import org.agty.sql.support.RowFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Forward-only cursor wrapper over JDBC ResultSet.
 */
public final class AgtySqlCursor implements AutoCloseable {

    private final ResultSet resultSet;
    private final Arguments arguments;
    private boolean closed;

    public AgtySqlCursor(ResultSet resultSet, Arguments arguments) {
        this.resultSet = resultSet;
        this.arguments = arguments;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public Statement getStatement() throws SQLException {
        return resultSet == null ? null : resultSet.getStatement();
    }

    public boolean isClosed() {
        return closed;
    }

    public SqlRow next() {
        if (closed || resultSet == null) {
            return null;
        }

        try {
            if (!resultSet.next()) {
                close();
                return null;
            }

            SqlRow row = RowFactory.newSqlRow();
            row.setDataIsString(arguments.convertValueToString());

            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); ++i) {
                row.setData(
                        metaData.getColumnName(i),
                        arguments.convertValueToString() ? resultSet.getString(i) : resultSet.getObject(i)
                );
            }

            return row;
        } catch (SQLException e) {
            throw new org.agty.sql.exceptions.AgtySqlException("AgtySqlCursor.next()", e.getMessage());
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }

        Statement statement = null;
        SQLException exception = null;

        try {
            statement = getStatement();
        } catch (SQLException e) {
            exception = e;
        }

        try {
            if (resultSet != null && !resultSet.isClosed()) {
                resultSet.close();
            }
        } catch (SQLException e) {
            if (exception == null) {
                exception = e;
            }
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

        closed = true;

        if (exception != null) {
            throw new org.agty.sql.exceptions.AgtySqlException("AgtySqlCursor.close()", exception.getMessage());
        }
    }
}
