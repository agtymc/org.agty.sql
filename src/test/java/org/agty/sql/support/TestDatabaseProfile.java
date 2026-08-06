package org.agty.sql.support;

import org.agty.sql.AgtySQL;
import org.agty.sql.driver.DialectCapabilities;

public record TestDatabaseProfile(
        String server,
        String tableName,
        String createTableDdl,
        String createAutoIdTableDdl,
        DialectCapabilities capabilities
) {

    public AgtySQL createSql() {
        AgtySQL sql = new AgtySQL(server);
        sql.getConfig().setThrowException(true);
        sql.getConfig().setDebug(false);
        return sql;
    }

    @Override
    public String toString() {
        return server;
    }
}
