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

    public AgtySqlStatement(Connection connection, AgtySqlConfig agtySqlConfig, int stmtRows) throws SQLException {
        this(connection, agtySqlConfig, stmtRows, 0);
    }

    public AgtySqlStatement(Connection connection, AgtySqlConfig agtySqlConfig, int stmtRows, int fetchSize) throws SQLException {
        this.connection = connection;
        this.agtySqlConfig = agtySqlConfig;
        this.stmtRows = stmtRows;
        this.fetchSize = fetchSize;
        createStatement();
    }

    public AgtySqlConfig getConfig() {
        return agtySqlConfig;
    }

    public Connection getConnection() {
        return connection;
    }

    public Statement getStatement() {
        return stmt;
    }

    public int getStmtRows() {
        return stmtRows;
    }

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
