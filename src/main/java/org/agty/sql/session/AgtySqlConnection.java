package org.agty.sql.session;

import org.agty.sql.config.AgtySqlConfig;
import org.agty.sql.support.DebugMessages;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ForkJoinPool;

/**
 * Creates a JDBC connection for the current AgtySQL session.
 */
public class AgtySqlConnection {
    private final AgtySqlConfig agtySqlConfig;
    private final String driver;

    public AgtySqlConnection(AgtySqlConfig agtySqlConfig, String driver) {
        this.agtySqlConfig = agtySqlConfig;
        this.driver = driver;
    }

    public AgtySqlConfig getConfig() {
        return agtySqlConfig;
    }

    private String getDriver() {
        return driver;
    }

    public Connection getConnection() throws SQLException {
        String connectionUri = getConnectionURI();
        debugMessage(connectionUri);

        Connection connection;
        if (isSqliteDriver()) {
            connection = DriverManager.getConnection(connectionUri);
        } else {
            connection = DriverManager.getConnection(
                    connectionUri,
                    getConfig().getUser(),
                    getConfig().getPassword()
            );
        }
        try {
            connection.setAutoCommit(getConfig().isAutoCommit());
            if (getConfig().getNetworkTimeoutMillis() > 0) {
                connection.setNetworkTimeout(ForkJoinPool.commonPool(), getConfig().getNetworkTimeoutMillis());
            }
            return connection;
        } catch (SQLException e) {
            try {
                connection.close();
            } catch (SQLException closeException) {
                e.addSuppressed(closeException);
            }
            throw e;
        }
    }

    private void debugMessage(String connectionUri) {
        if (getConfig().isDebug()) {
            DebugMessages.print("AgtySqlConnection", connectionUri + "&user=" + getConfig().getUser() + "&password=hidden");
        }
    }

    String getConnectionURI() {
        if (isSqliteDriver()) {
            return getSqliteConnectionUri();
        }

        if (isH2Driver()) {
            return getH2ConnectionUri();
        }

        if (isSqlServerDriver()) {
            return getSqlServerConnectionUri();
        }

        StringBuilder connectURI = new StringBuilder();

        connectURI.append("jdbc:");
        connectURI.append(getDriver());
        connectURI.append("://");
        connectURI.append(getConfig().getServer());
        connectURI.append(':');
        connectURI.append(getConfig().getPort());
        connectURI.append('/');

        if (getConfig().isDatabase()) {
            connectURI.append(getConfig().getDatabase());
        }

        connectURI.append("?serverTimezone=");
        connectURI.append(getConfig().getTimeZone());
        connectURI.append("&useUnicode=true");
        connectURI.append("&characterEncoding=");
        connectURI.append(getConfig().getEncoding());
        connectURI.append("&characterSetResults=");
        connectURI.append(getConfig().getEncoding());

        appendNetworkProperties(connectURI);

        if (getConfig().isSchema()) {
            connectURI.append("&currentSchema=");
            connectURI.append("\"");
            connectURI.append(getConfig().getSchema());
            connectURI.append("\"");
        }

        return connectURI.toString();
    }

    private boolean isSqliteDriver() {
        return "sqlite".equalsIgnoreCase(getDriver());
    }

    private boolean isH2Driver() {
        return "h2".equalsIgnoreCase(getDriver());
    }

    private boolean isSqlServerDriver() {
        return "sqlserver".equalsIgnoreCase(getDriver());
    }

    private String getSqliteConnectionUri() {
        return "jdbc:sqlite:" + normalizeDatabasePath();
    }

    private String getH2ConnectionUri() {
        return "jdbc:h2:file:" + normalizeDatabasePath() + ";MODE=MySQL";
    }

    private String getSqlServerConnectionUri() {
        StringBuilder connectURI = new StringBuilder();

        connectURI.append("jdbc:sqlserver://");
        connectURI.append(getConfig().getServer());

        if (getConfig().getPort() > 0) {
            connectURI.append(':');
            connectURI.append(getConfig().getPort());
        }

        connectURI.append(";");

        if (getConfig().isDatabase()) {
            connectURI.append("databaseName=");
            connectURI.append(getConfig().getDatabase());
            connectURI.append(";");
        }

        connectURI.append("encrypt=true;");
        connectURI.append("trustServerCertificate=");
        connectURI.append(getConfig().isTrustServerCertificate());
        connectURI.append(';');

        if (getConfig().getLoginTimeoutSeconds() > 0) {
            connectURI.append("loginTimeout=");
            connectURI.append(getConfig().getLoginTimeoutSeconds());
            connectURI.append(';');
        }
        if (getConfig().getNetworkTimeoutMillis() > 0) {
            connectURI.append("socketTimeout=");
            connectURI.append(getConfig().getNetworkTimeoutMillis());
            connectURI.append(';');
        }

        return connectURI.toString();
    }

    private void appendNetworkProperties(StringBuilder connectURI) {
        int loginTimeoutSeconds = getConfig().getLoginTimeoutSeconds();
        int networkTimeoutMillis = getConfig().getNetworkTimeoutMillis();

        if (loginTimeoutSeconds > 0) {
            connectURI.append("&connectTimeout=");
            connectURI.append("postgresql".equalsIgnoreCase(getDriver())
                    ? loginTimeoutSeconds
                    : loginTimeoutSeconds * 1000L);
            if ("postgresql".equalsIgnoreCase(getDriver())) {
                connectURI.append("&loginTimeout=");
                connectURI.append(loginTimeoutSeconds);
            }
        }
        if (networkTimeoutMillis > 0) {
            connectURI.append("&socketTimeout=");
            connectURI.append("postgresql".equalsIgnoreCase(getDriver())
                    ? Math.max(1L, (networkTimeoutMillis + 999L) / 1000L)
                    : networkTimeoutMillis);
        }
    }

    private String normalizeDatabasePath() {
        Path path = Paths.get(getConfig().getDatabase());
        return path.isAbsolute() ? path.toString() : path.toAbsolutePath().normalize().toString();
    }
}
