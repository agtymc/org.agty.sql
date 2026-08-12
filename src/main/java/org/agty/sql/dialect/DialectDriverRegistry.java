package org.agty.sql.dialect;

import org.agty.sql.AgtySQL;
import org.agty.sql.dialect.emptysql.EmptySQL;
import org.agty.sql.dialect.h2.H2;
import org.agty.sql.dialect.mariadb.MariaDB;
import org.agty.sql.dialect.mssql.MsSQL;
import org.agty.sql.dialect.mysql.MySQL;
import org.agty.sql.dialect.pgsql.PgSQL;
import org.agty.sql.dialect.sqlite.SQLite;
import org.agty.sql.exceptions.SqlDriverNotFoundException;
import org.agty.sql.interfaces.Sql;

/**
 * Internal registry for concrete dialect implementations.
 */
public final class DialectDriverRegistry {

    private DialectDriverRegistry() {
    }

    public static Sql getDialect(String driver, AgtySQL agtySQL) {
        switch (driver.toLowerCase()) {
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
            case "emptysql" -> {
                return new EmptySQL(agtySQL);
            }
        }

        throw new SqlDriverNotFoundException(driver);
    }

    public static String getDriverName(String driver) {
        return getDialect(driver, null).getDriverName();
    }
}
