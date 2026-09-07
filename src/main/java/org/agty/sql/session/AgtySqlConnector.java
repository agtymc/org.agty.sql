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

    /**
     * Creates a new instance.
     */
    public AgtySqlConnector() {
        configInit("default");
    }

    /**
     * Creates a new instance.
     * @param server parameter value
     */
    public AgtySqlConnector(String server) {
        configInit(server);
    }

    /**
     * Creates a new instance.
     * @param server parameter value
     * @param path parameter value
     */
    public AgtySqlConnector(String server, String path) {
        configInit(server, path);
    }

    /**
     * Creates a new instance.
     * @param agtySqlConfig parameter value
     */
    public AgtySqlConnector(AgtySqlConfig agtySqlConfig) {
        configInit(agtySqlConfig);
    }

    /**
     * Creates a session around an already leased JDBC connection.
     * Closing this connector closes the lease, not a pool-owned physical connection.
     * @param agtySqlConfig parameter value
     * @param connection parameter value
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

    /**
     * Sets the no closable.
     * @param noClosable parameter value
     */
    public void setNoClosable(boolean noClosable) {
        this.noClosable = noClosable;
    }

    /**
     * Returns whether no closable.
     * @return operation result
     */
    public boolean isNoClosable() {
        return noClosable;
    }

    /**
     * Returns the config.
     * @return operation result
     */
    public AgtySqlConfig getConfig() {
        return config;
    }

    /**
     * Returns whether check config.
     * @return operation result
     */
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

    /**
     * Returns the connection.
     * @return operation result
     * @throws SQLException if the operation cannot be completed
     */
    public Connection getConnection() throws SQLException {
        if (connection == null) {
            if (externallyManagedConnection) {
                throw new SQLException("Leased connection is closed");
            }
            connection = createConnection();
        }
        return connection;
    }

    /**
     * Performs the check and reconnect connection operation.
     * @throws SQLException if the operation cannot be completed
     */
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

    /**
     * Returns the statement.
     * @return operation result
     * @throws SQLException if the operation cannot be completed
     */
    public Statement getStatement() throws SQLException {
        checkAndReconnectConnection();
        return new AgtySqlStatement(getConnection(), getConfig(), getStmtRows(), getFetchSize()).getStatement();
    }

    /**
     * Sets the fetch size.
     * @param fetchSize parameter value
     */
    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    /**
     * Returns the fetch size.
     * @return operation result
     */
    public int getFetchSize() {
        return fetchSize;
    }

    /**
     * Performs the prepare statement operation.
     * @param query parameter value
     * @return operation result
     * @throws SQLException if the operation cannot be completed
     */
    public PreparedStatement prepareStatement(String query) throws SQLException {
        checkAndReconnectConnection();
        return getConnection().prepareStatement(query);
    }

    /**
     * Performs the prepare statement operation.
     * @param query parameter value
     * @param autoGeneratedKeys parameter value
     * @return operation result
     * @throws SQLException if the operation cannot be completed
     */
    public PreparedStatement prepareStatement(String query, int autoGeneratedKeys) throws SQLException {
        checkAndReconnectConnection();
        return getConnection().prepareStatement(query, autoGeneratedKeys);
    }

    /**
     * Performs the create batch statement operation.
     * @return operation result
     * @throws SQLException if the operation cannot be completed
     */
    public Statement createBatchStatement() throws SQLException {
        checkAndReconnectConnection();
        return new AgtySqlStatement(getConnection(), getConfig(), getStmtRows(), getFetchSize()).getStatement();
    }

    /**
     * Returns whether auto commit.
     * @return operation result
     * @throws SQLException if the operation cannot be completed
     */
    public boolean isAutoCommit() throws SQLException {
        return getConnection().getAutoCommit();
    }

    /**
     * Sets the auto commit.
     * @param commit parameter value
     */
    public void setAutoCommit(Boolean commit) {
        try {
            getConnection().setAutoCommit(commit);
        } catch (SQLException e) {
            throw new AgtySqlException("AgtySqlConnector.setAutoCommit()", e.getMessage(), e);
        }
    }

    /**
     * Performs the commit operation.
     * @throws SQLException if the operation cannot be completed
     */
    public void commit() throws SQLException {
        getConnection().commit();
    }

    /**
     * Performs the rollback operation.
     * @throws SQLException if the operation cannot be completed
     */
    public void rollback() throws SQLException {
        getConnection().rollback();
    }

    /**
     * Performs the close operation.
     * @throws SQLException if the operation cannot be completed
     */
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

    /**
     * Returns the stmt rows.
     * @return operation result
     */
    public int getStmtRows() {
        if (stmtRows > 0) {
            return stmtRows;
        }
        if (getConfig().getStmtRows() > 0) {
            return getConfig().getStmtRows();
        }
        return DEFAULT_STATEMENT_ROWS;
    }

    /**
     * Sets the stmt rows.
     * @param stmtRows parameter value
     */
    public void setStmtRows(int stmtRows) {
        this.stmtRows = stmtRows;
    }

    private void debugMessage(String type, String message) {
        if (getConfig().isDebug()) {
            DebugMessages.print(type, message);
        }
    }
}
