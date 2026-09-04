package org.agty.sql.datasource;

import org.agty.sql.config.AgtySqlConfig;
import org.agty.sql.dialect.DialectDriverRegistry;
import org.agty.sql.session.AgtySqlConnection;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Standard JDBC DataSource implementation backed by AgtySqlConfig.
 */
public class AgtySqlDataSource implements DataSource {

    private final AgtySqlConfig config;
    private volatile PrintWriter logWriter;
    private volatile int loginTimeout;

    public AgtySqlDataSource(AgtySqlConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("AgtySqlConfig must not be null");
        }
        this.config = AgtySqlConfig.getClone(config);
        this.loginTimeout = config.getLoginTimeoutSeconds();
    }

    public AgtySqlConfig getConfig() {
        return AgtySqlConfig.getClone(config);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return new AgtySqlConnection(connectionConfig(config), resolveDriverName()).getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        AgtySqlConfig overriddenConfig = AgtySqlConfig.getClone(config)
                .setUser(username)
                .setPassword(password);
        return new AgtySqlConnection(connectionConfig(overriddenConfig), resolveDriverName()).getConnection();
    }

    @Override
    public PrintWriter getLogWriter() {
        return logWriter;
    }

    @Override
    public void setLogWriter(PrintWriter out) {
        this.logWriter = out;
    }

    @Override
    public void setLoginTimeout(int seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("Login timeout must not be negative");
        }
        this.loginTimeout = seconds;
    }

    @Override
    public int getLoginTimeout() {
        return loginTimeout;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Parent logger is not supported");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return iface.cast(this);
        }
        throw new SQLException("Unsupported unwrap: " + iface.getName());
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isInstance(this);
    }

    private String resolveDriverName() {
        return DialectDriverRegistry.getDriverName(config.getDriver());
    }

    private AgtySqlConfig connectionConfig(AgtySqlConfig source) {
        return AgtySqlConfig.getClone(source).setLoginTimeoutSeconds(loginTimeout);
    }
}
