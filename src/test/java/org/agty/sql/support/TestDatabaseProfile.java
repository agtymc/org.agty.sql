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

    public String sqlBooleanLiteral(boolean value) {
        if ("mssql".equalsIgnoreCase(server)) {
            return value ? "1" : "0";
        }
        return value ? "true" : "false";
    }

    @Override
    public String toString() {
        return server;
    }
}
