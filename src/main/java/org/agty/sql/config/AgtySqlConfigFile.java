package org.agty.sql.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * SQL config from file properties
 */
public class AgtySqlConfigFile {
    private final AgtySqlConfig agtySqlConfig = new AgtySqlConfig();

    /**
     * Constructor
     * @param server A server section
     * @param path Properties file
     */
    public AgtySqlConfigFile(String server, String path) {
        init(server, path);
    }

    /**
     * Init and lock the config
     * @param server A server section
     * @param path Properties file
     */
    private void init(String server, String path) {
        String configSection = "db." + server + ".";
        Properties propertyFile = new Properties();

        try (FileInputStream input = new FileInputStream(path)) {
            propertyFile.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load SQL config: " + path, e);
        }

        agtySqlConfig.setDriver(getProperty(propertyFile, configSection + "driver", "mysql"));
        agtySqlConfig.setServer(getProperty(propertyFile, configSection + "server", "localhost"));
        agtySqlConfig.setPort(Integer.parseInt(getProperty(propertyFile, configSection + "port", "3306")));
        agtySqlConfig.setUser(getProperty(propertyFile, configSection + "user", null));
        agtySqlConfig.setPassword(getProperty(propertyFile, configSection + "password", null));
        agtySqlConfig.setOwner(getProperty(propertyFile, configSection + "owner", agtySqlConfig.getUser()));
        agtySqlConfig.setDatabase(getProperty(propertyFile, configSection + "database", agtySqlConfig.getDatabase()));
        agtySqlConfig.setSchema(getProperty(propertyFile, configSection + "schema", agtySqlConfig.getSchema()));
        agtySqlConfig.setPfx(getProperty(propertyFile, configSection + "pfx", ""));
        agtySqlConfig.setEncoding(getProperty(propertyFile, configSection + "encoding", "UTF-8"));
        agtySqlConfig.setTimeZone(getProperty(propertyFile, configSection + "serverTimeZone", "UTC"));
        agtySqlConfig.setAutoCommit(Boolean.parseBoolean(
                getProperty(propertyFile, configSection + "autoCommit", "true")
        ));
        agtySqlConfig.setTrustServerCertificate(Boolean.parseBoolean(
                getProperty(propertyFile, configSection + "trustServerCertificate", "false")
        ));
        agtySqlConfig.setLogQuery(Boolean.parseBoolean(
                getProperty(propertyFile, configSection + "logquery", "false")
        ));
        agtySqlConfig.setLogQueryValues(Boolean.parseBoolean(
                getProperty(propertyFile, configSection + "logQueryValues", "false")
        ));
        agtySqlConfig.setLogQueryFile(getProperty(propertyFile, configSection + "logQueryFile", null));
        agtySqlConfig.setLogErrors(Boolean.parseBoolean(
                getProperty(propertyFile, configSection + "logErrors", "false")
        ));
        agtySqlConfig.setLogErrorsFile(getProperty(propertyFile, configSection + "logErrorsFile", null));
        agtySqlConfig.setDebug(Boolean.parseBoolean(
                getProperty(propertyFile, configSection + "debug", "false")
        ));
        agtySqlConfig.setThrowException(Boolean.parseBoolean(
                getProperty(propertyFile, configSection + "throwException", "true")
        ));
        agtySqlConfig.setNoRequery(Boolean.parseBoolean(
                getProperty(propertyFile, configSection + "noRequery", "false")
        ));
        agtySqlConfig.setStmtRows(Integer.parseInt(
                getProperty(propertyFile, configSection + "stmtRows", "100")
        ));
        agtySqlConfig.setLoginTimeoutSeconds(Integer.parseInt(
                getProperty(propertyFile, configSection + "loginTimeoutSeconds", "0")
        ));
        agtySqlConfig.setNetworkTimeoutMillis(Integer.parseInt(
                getProperty(propertyFile, configSection + "networkTimeoutMillis", "0")
        ));
    }

    private String getProperty(Properties properties, String key, String defaultValue) {
        return resolveValue(properties.getProperty(key, defaultValue));
    }

    private String resolveValue(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.startsWith("${") && trimmed.endsWith("}") && trimmed.length() > 3) {
            String variable = trimmed.substring(2, trimmed.length() - 1);
            return System.getenv(variable);
        }
        return value;
    }

    /**
     * Get the config
     * @return builder
     */
    public AgtySqlConfig getConfig() {
        return agtySqlConfig;
    }
}
