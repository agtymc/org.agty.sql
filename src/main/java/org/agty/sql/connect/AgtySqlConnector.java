package org.agty.sql.connect;

import org.agty.sql.config.AgtySqlConfig;

/**
 * @deprecated use {@link org.agty.sql.session.AgtySqlConnector}.
 */
@Deprecated
public class AgtySqlConnector extends org.agty.sql.session.AgtySqlConnector {

    public AgtySqlConnector() {
        super();
    }

    public AgtySqlConnector(String server) {
        super(server);
    }

    public AgtySqlConnector(String server, String path) {
        super(server, path);
    }

    public AgtySqlConnector(AgtySqlConfig agtySqlConfig) {
        super(agtySqlConfig);
    }
}
