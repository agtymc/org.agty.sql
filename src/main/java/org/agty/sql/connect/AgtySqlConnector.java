package org.agty.sql.connect;

import org.agty.sql.config.AgtySqlConfig;

import java.sql.Connection;

/**
 * @deprecated use {@link org.agty.sql.session.AgtySqlConnector}.
 */
@Deprecated
public class AgtySqlConnector extends org.agty.sql.session.AgtySqlConnector {

    /**
     * Creates a new instance.
     */
    public AgtySqlConnector() {
        super();
    }

    /**
     * Creates a new instance.
     * @param server parameter value
     */
    public AgtySqlConnector(String server) {
        super(server);
    }

    /**
     * Creates a new instance.
     * @param server parameter value
     * @param path parameter value
     */
    public AgtySqlConnector(String server, String path) {
        super(server, path);
    }

    /**
     * Creates a new instance.
     * @param agtySqlConfig parameter value
     */
    public AgtySqlConnector(AgtySqlConfig agtySqlConfig) {
        super(agtySqlConfig);
    }

    /**
     * Creates a new instance.
     * @param agtySqlConfig parameter value
     * @param connection parameter value
     */
    public AgtySqlConnector(AgtySqlConfig agtySqlConfig, Connection connection) {
        super(agtySqlConfig, connection);
    }
}
