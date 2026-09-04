package org.agty.sql.session;

import org.agty.sql.AgtySQL;
import org.agty.sql.config.AgtySqlConfig;
import org.agty.sql.config.AgtySqlConfigInit;
import org.agty.sql.dialect.DialectDriverRegistry;
import org.agty.sql.exceptions.AgtySqlException;
import org.agty.sql.support.DebugMessages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Owns connection lifecycle for an AgtySQL session.
 */
public class AgtySqlConnector {

    private static final int DEFAULT_STATEMENT_ROWS = 100;
    private int stmtRows;
    private AgtySqlConfig config;
    private Connection connection;
    private boolean externallyManagedConnection;
    private int fetchSize;
    private boolean noClosable;

    public AgtySqlConnector() {
        configInit("default");
    }

    public AgtySqlConnector(String server) {
        configInit(server);
    }

    public AgtySqlConnector(String server, String path) {
        configInit(server, path);
    }

    public AgtySqlConnector(AgtySqlConfig agtySqlConfig) {
        configInit(agtySqlConfig);
    }

    /**
     * Creates a session around an already leased JDBC connection.
     * Closing this connector closes the lease, not a pool-owned physical connection.
     */
    public AgtySqlConnector(AgtySqlConfig agtySqlConfig, Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection must not be null");
        }
        configInit(agtySqlConfig);
        this.connection = connection;
        this.externallyManagedConnection = true;
    }

    private void configInit(String server) {
        if (config == null) {
            config = new AgtySqlConfigInit(server).getConfig();
        }
    }

    private void configInit(String server, String path) {
        if (config == null) {
            config = new AgtySqlConfigInit(server, path).getConfig();
        }
    }

    private void configInit(AgtySqlConfig agtySqlConfig) {
        if (config == null) {
            config = AgtySqlConfig.getClone(agtySqlConfig);
        }
    }

    public void setNoClosable(boolean noClosable) {
        this.noClosable = noClosable;
    }

    public boolean isNoClosable() {
        return noClosable;
    }

    public AgtySqlConfig getConfig() {
        return config;
    }

    public boolean isCheckConfig() {
        String driver = getConfig().getDriver();

        if ("sqlite".equalsIgnoreCase(driver) || "h2".equalsIgnoreCase(driver)) {
            return hasText(getConfig().getDatabase());
        }

        return hasText(getConfig().getUser())
                && hasText(getConfig().getPassword())
                && hasText(getConfig().getServer())
                && getConfig().getPort() > 0;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String getDriverName() {
        return DialectDriverRegistry.getDriverName(getConfig().getDriver());
    }

    public Connection getConnection() throws SQLException {
        if (connection == null) {
            if (externallyManagedConnection) {
                throw new SQLException("Leased connection is closed");
            }
            connection = createConnection();
        }
        return connection;
    }

    public void checkAndReconnectConnection() throws SQLException {
        if (connection == null) {
            return;
        }

        String driver = getConfig().getDriver();
        if ("sqlite".equalsIgnoreCase(driver) || "h2".equalsIgnoreCase(driver)) {
            if (connection.isClosed()) {
                if (externallyManagedConnection) {
                    throw new SQLException("Leased connection is closed");
                }
                connection = createConnection();
            }
            return;
        }

        if (!connection.isValid(100)) {
            connection.close();
            if (externallyManagedConnection) {
                throw new SQLException("Leased connection is not valid");
            }
            connection = createConnection();
        }
    }

    private Connection createConnection() throws SQLException {
        debugMessage("AgtySqlConnector.createConnect()", "v." + AgtySQL.VERSION + "; server: " + getConfig().getDriver());

        if (!isCheckConfig()) {
            throw new AgtySqlException(
                    "AgtySqlConnector.createConnect()",
                    "Missing required database, server, port, user, or password configuration"
            );
        }

        return new AgtySqlConnection(getConfig(), getDriverName()).getConnection();
    }

    public Statement getStatement() throws SQLException {
        checkAndReconnectConnection();
        return new AgtySqlStatement(getConnection(), getConfig(), getStmtRows(), getFetchSize()).getStatement();
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    public int getFetchSize() {
        return fetchSize;
    }

    public PreparedStatement prepareStatement(String query) throws SQLException {
        checkAndReconnectConnection();
        return getConnection().prepareStatement(query);
    }

    public PreparedStatement prepareStatement(String query, int autoGeneratedKeys) throws SQLException {
        checkAndReconnectConnection();
        return getConnection().prepareStatement(query, autoGeneratedKeys);
    }

    public Statement createBatchStatement() throws SQLException {
        checkAndReconnectConnection();
        return new AgtySqlStatement(getConnection(), getConfig(), getStmtRows(), getFetchSize()).getStatement();
    }

    public boolean isAutoCommit() throws SQLException {
        return getConnection().getAutoCommit();
    }

    public void setAutoCommit(Boolean commit) {
        try {
            getConnection().setAutoCommit(commit);
        } catch (SQLException e) {
            throw new AgtySqlException("AgtySqlConnector.setAutoCommit()", e.getMessage(), e);
        }
    }

    public void commit() throws SQLException {
        getConnection().commit();
    }

    public void rollback() throws SQLException {
        getConnection().rollback();
    }

    public void close() throws SQLException {
        if (isNoClosable()) {
            return;
        }

        if (connection != null) {
            getConnection().close();
        }

        connection = null;

        debugMessage("AgtySqlConnector.close()", getConfig().getServer() + "/" + getConfig().getDriver() + " -> Connect closed");
    }

    public int getStmtRows() {
        if (stmtRows > 0) {
            return stmtRows;
        }
        if (getConfig().getStmtRows() > 0) {
            return getConfig().getStmtRows();
        }
        return DEFAULT_STATEMENT_ROWS;
    }

    public void setStmtRows(int stmtRows) {
        this.stmtRows = stmtRows;
    }

    private void debugMessage(String type, String message) {
        if (getConfig().isDebug()) {
            DebugMessages.print(type, message);
        }
    }
}
