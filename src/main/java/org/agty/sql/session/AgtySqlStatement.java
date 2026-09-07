package org.agty.sql.session;

import org.agty.sql.config.AgtySqlConfig;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates a statement bound to the current session connection.
 */
public class AgtySqlStatement {
    private final Connection connection;
    private final AgtySqlConfig agtySqlConfig;
    private final int stmtRows;
    private final int fetchSize;
    private Statement stmt;

    /**
     * Creates a new instance.
     * @param connection parameter value
     * @param agtySqlConfig parameter value
     * @param stmtRows parameter value
     * @throws SQLException if the operation cannot be completed
     */
    public AgtySqlStatement(Connection connection, AgtySqlConfig agtySqlConfig, int stmtRows) throws SQLException {
        this(connection, agtySqlConfig, stmtRows, 0);
    }

    /**
     * Creates a new instance.
     * @param connection parameter value
     * @param agtySqlConfig parameter value
     * @param stmtRows parameter value
     * @param fetchSize parameter value
     * @throws SQLException if the operation cannot be completed
     */
    public AgtySqlStatement(Connection connection, AgtySqlConfig agtySqlConfig, int stmtRows, int fetchSize) throws SQLException {
        this.connection = connection;
        this.agtySqlConfig = agtySqlConfig;
        this.stmtRows = stmtRows;
        this.fetchSize = fetchSize;
        createStatement();
    }

    /**
     * Returns the config.
     * @return operation result
     */
    public AgtySqlConfig getConfig() {
        return agtySqlConfig;
    }

    /**
     * Returns the connection.
     * @return operation result
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Returns the statement.
     * @return operation result
     */
    public Statement getStatement() {
        return stmt;
    }

    /**
     * Returns the stmt rows.
     * @return operation result
     */
    public int getStmtRows() {
        return stmtRows;
    }

    /**
     * Returns the fetch size.
     * @return operation result
     */
    public int getFetchSize() {
        return fetchSize;
    }

    private void createStatement() throws SQLException {
        stmt = connection.createStatement(
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY
        );

        stmt.setFetchSize(fetchSize > 0 ? fetchSize : stmtRows);
    }
}
