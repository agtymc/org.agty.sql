package org.agty.sql.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ListResultSet implements AutoCloseable {
    private final ResultSet resultSet;

    public ListResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public Statement getStatement() throws SQLException {
        return resultSet == null ? null : resultSet.getStatement();
    }

    @Override
    public void close() throws SQLException {
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
            throw exception;
        }
    }
}
