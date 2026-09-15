package org.agty.sql.dialect;

import org.agty.sql.AgtySQL;
import org.agty.sql.dialect.clickhouse.ClickHouse;
import org.agty.sql.dialect.emptysql.EmptySQL;
import org.agty.sql.dialect.h2.H2;
import org.agty.sql.dialect.mariadb.MariaDB;
import org.agty.sql.dialect.mssql.MsSQL;
import org.agty.sql.dialect.mysql.MySQL;
import org.agty.sql.dialect.pgsql.PgSQL;
import org.agty.sql.dialect.sqlite.SQLite;
import org.agty.sql.exceptions.SqlDriverNotFoundException;
import org.agty.sql.interfaces.Sql;

import java.util.Locale;

/**
 * Internal registry for concrete dialect implementations.
 */
public final class DialectDriverRegistry {

    private DialectDriverRegistry() {
    }

    /**
     * Returns the dialect.
     * @param driver parameter value
     * @param agtySQL parameter value
     * @return operation result
     */
    public static Sql getDialect(String driver, AgtySQL agtySQL) {
        switch (driver.toLowerCase(Locale.ROOT)) {
            case "mysql" -> {
                return new MySQL(agtySQL);
            }
            case "mariadb" -> {
                return new MariaDB(agtySQL);
            }
            case "pgsql" -> {
                return new PgSQL(agtySQL);
            }
            case "sqlite" -> {
                return new SQLite(agtySQL);
            }
            case "h2" -> {
                return new H2(agtySQL);
            }
            case "mssql" -> {
                return new MsSQL(agtySQL);
            }
            case "clickhouse" -> {
                return new ClickHouse(agtySQL);
            }
            case "emptysql" -> {
                return new EmptySQL(agtySQL);
            }
        }

        throw new SqlDriverNotFoundException(driver);
    }

    /**
     * Returns the driver name.
     * @param driver parameter value
     * @return operation result
     */
    public static String getDriverName(String driver) {
        return getDialect(driver, null).getDriverName();
    }
}
