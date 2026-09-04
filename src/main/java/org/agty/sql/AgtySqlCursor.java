package org.agty.sql;

import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.data.Arguments;
import org.agty.sql.support.RowFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;

/**
 * Forward-only cursor wrapper over JDBC ResultSet.
 *
 * <p>A cursor is stateful and not thread-safe. Consume and close it from one
 * thread.</p>
 */
public final class AgtySqlCursor implements AutoCloseable {

    private final ResultSet resultSet;
    private final Arguments arguments;
    private final Consumer<AgtySqlCursor> closeCallback;
    private SqlRow bufferedRow;
    private boolean nextRowBuffered;
    private boolean closed;

    public AgtySqlCursor(ResultSet resultSet, Arguments arguments) {
        this(resultSet, arguments, null);
    }

    AgtySqlCursor(
            ResultSet resultSet,
            Arguments arguments,
            Consumer<AgtySqlCursor> closeCallback
    ) {
        this.resultSet = resultSet;
        this.arguments = arguments;
        this.closeCallback = closeCallback;
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

    public boolean hasNext() {
        if (closed || resultSet == null) {
            return false;
        }

        if (nextRowBuffered) {
            return true;
        }

        bufferedRow = readNextRow();
        if (bufferedRow == null) {
            return false;
        }

        nextRowBuffered = true;
        return true;
    }

    public SqlRow next() {
        if (closed || resultSet == null) {
            return null;
        }

        if (nextRowBuffered) {
            SqlRow row = bufferedRow;
            bufferedRow = null;
            nextRowBuffered = false;
            return row;
        }

        return readNextRow();
    }

    private SqlRow readNextRow() {
        try {
            if (!resultSet.next()) {
                close();
                return null;
            }

            SqlRow row = RowFactory.newSqlRow();
            row.setValuesAsString(arguments.convertValueToString());

            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); ++i) {
                row.setData(
                        metaData.getColumnLabel(i),
                        arguments.convertValueToString() ? resultSet.getString(i) : resultSet.getObject(i)
                );
            }

            return row;
        } catch (SQLException e) {
            try {
                close();
            } catch (RuntimeException closeException) {
                e.addSuppressed(closeException);
            }
            throw new org.agty.sql.exceptions.AgtySqlException(
                    "AgtySqlCursor.next()",
                    e.getMessage(),
                    e
            );
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
        if (closeCallback != null) {
            closeCallback.accept(this);
        }

        if (exception != null) {
            throw new org.agty.sql.exceptions.AgtySqlException(
                    "AgtySqlCursor.close()",
                    exception.getMessage(),
                    exception
            );
        }
    }
}
