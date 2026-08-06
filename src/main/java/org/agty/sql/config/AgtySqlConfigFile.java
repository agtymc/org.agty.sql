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

        try {
            propertyFile.load(new FileInputStream(path));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        agtySqlConfig.setDriver(propertyFile.getProperty(configSection + "driver", "mysql"));
        agtySqlConfig.setServer(propertyFile.getProperty(configSection + "server", "localhost"));
        agtySqlConfig.setPort(Integer.parseInt(propertyFile.getProperty(configSection + "port", "3306")));
        agtySqlConfig.setUser(propertyFile.getProperty(configSection + "user", "root"));
        agtySqlConfig.setPassword(propertyFile.getProperty(configSection + "password", "root"));
        agtySqlConfig.setOwner(propertyFile.getProperty(configSection + "owner", agtySqlConfig.getUser()));
        agtySqlConfig.setDatabase(propertyFile.getProperty(configSection + "database", agtySqlConfig.getDatabase()));
        agtySqlConfig.setSchema(propertyFile.getProperty(configSection + "schema", agtySqlConfig.getSchema()));
        agtySqlConfig.setPfx(propertyFile.getProperty(configSection + "pfx", ""));
        agtySqlConfig.setEncoding(propertyFile.getProperty(configSection + "encoding", "UTF-8"));
        agtySqlConfig.setTimeZone(propertyFile.getProperty(configSection + "serverTimeZone", "UTC"));
        agtySqlConfig.setAutoCommit(Boolean.parseBoolean(propertyFile.getProperty(configSection + "autoCommit", "true")));
        agtySqlConfig.setLogQuery(Boolean.parseBoolean(propertyFile.getProperty(configSection + "logquery", "false")));
        agtySqlConfig.setDebug(Boolean.parseBoolean(propertyFile.getProperty(configSection + "debug", "false")));
        agtySqlConfig.setThrowException(Boolean.parseBoolean(propertyFile.getProperty(configSection + "throwException", "false")));
        agtySqlConfig.setNoRequery(Boolean.parseBoolean(propertyFile.getProperty(configSection + "noRequery", "false")));
        agtySqlConfig.setStmtRows(Integer.parseInt(propertyFile.getProperty(configSection + "stmtRows", "100")));
    }

    /**
     * Get the config
     * @return builder
     */
    public AgtySqlConfig getConfig() {
        return agtySqlConfig;
    }
}
