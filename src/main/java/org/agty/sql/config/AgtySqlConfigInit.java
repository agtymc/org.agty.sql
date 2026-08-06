package org.agty.sql.config;

/**
 * SQL Config init.
 * Load all properties from a file or from a sql config.
 * (driver, server, user, password, etc.)
 */
public class AgtySqlConfigInit {
    public static final String DEFAULT_CONFIG_PATH = "config.ini";

    private AgtySqlConfig agtySqlConfig;

    /**
     * Constructor.
     */
    public AgtySqlConfigInit() {}

    /**
     * Constructor.
     * @param server Server section into property file
     */
    public AgtySqlConfigInit(String server) {
        initFromFile(server, DEFAULT_CONFIG_PATH);
    }

    /**
     * Constructor.
     * @param server Server section into property file
     * @param path Properties file
     */
    public AgtySqlConfigInit(String server, String path) {
        initFromFile(server, path);
    }

    /**
     * Init by a file.
     * @param server имя сервера.
     * @param path путь к файлу.
     */
    private void initFromFile(String server, String path) {
        agtySqlConfig = new AgtySqlConfigFile(server, path).getConfig();
    }

    /**
     * Get a config.
     * @return builder
     */
    public AgtySqlConfig getConfig() {
        return agtySqlConfig;
    }
}
