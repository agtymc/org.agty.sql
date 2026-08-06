package org.agty.sql.session;

import org.agty.sql.config.AgtySqlConfig;
import org.agty.sql.support.DebugMessages;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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

        if (isSqliteDriver()) {
            return DriverManager.getConnection(connectionUri);
        }

        return DriverManager.getConnection(connectionUri, getConfig().getUser(), getConfig().getPassword());
    }

    private void debugMessage(String connectionUri) {
        if (getConfig().isDebug()) {
            DebugMessages.print("AgtySqlConnection", connectionUri + "&user=" + getConfig().getUser() + "&password=hidden");
        }
    }

    private String getConnectionURI() {
        if (isSqliteDriver()) {
            return getSqliteConnectionUri();
        }

        if (isH2Driver()) {
            return getH2ConnectionUri();
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

    private String getSqliteConnectionUri() {
        return "jdbc:sqlite:" + normalizeDatabasePath();
    }

    private String getH2ConnectionUri() {
        return "jdbc:h2:file:" + normalizeDatabasePath() + ";MODE=MySQL";
    }

    private String normalizeDatabasePath() {
        Path path = Paths.get(getConfig().getDatabase());
        return path.isAbsolute() ? path.toString() : path.toAbsolutePath().normalize().toString();
    }
}
