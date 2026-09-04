package org.agty.sql.dialect.sqlite;

import org.agty.sql.AgtySQL;
import org.agty.sql.data.Arguments;
import org.agty.sql.data.SqlExpression;
import org.agty.sql.driver.DialectCapabilities;
import org.agty.sql.driver.LastInsertIdStrategy;
import org.agty.sql.driver.UpdateAndGetStrategy;
import org.agty.sql.driver.WriteReturnStrategy;
import org.agty.sql.dialect.mysql.MySQL;
import org.agty.sql.interfaces.SqlRow;

public class SQLite extends MySQL {

    private static final String DRIVER = "sqlite";

    public SQLite(AgtySQL agtySQL) {
        super(agtySQL);
    }

    @Override
    public String getDriverName() {
        return DRIVER;
    }

    @Override
    public DialectCapabilities getCapabilities() {
        return DialectCapabilities.of(
                true,
                false,
                LastInsertIdStrategy.CONNECTION_FUNCTION,
                WriteReturnStrategy.FOLLOW_UP_FETCH,
                UpdateAndGetStrategy.FOLLOW_UP_FETCH_BY_WHERE
        );
    }

    @Override
    public boolean tableIsExists(String table) {
        SqlRow fetch = getAgtySQL().fetch(
                new Arguments()
                        .useStatementPrepare(true)
                        .setQuery(
                                "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ? LIMIT 1",
                                table
                        )
                        .setNoRebuildQuery(true)
        );
        return fetch.isSet("name");
    }

    @Override
    public String truncateQuery(String table) {
        return "DELETE FROM `" + table + "`";
    }

    @Override
    public Long getLastInsertId(String table, String primaryKey) {
        SqlRow row = getAgtySQL().fetch(
                new Arguments()
                        .setQuery(SqlExpression.trusted("SELECT last_insert_rowid() AS last_id"))
                        .setNoRebuildQuery(true)
        );
        return row.getLong("last_id");
    }

    @Override
    public String getPrimaryKey(String table) {
        try {
            String tableName = getAgtySQL().rebuildTable(table);
            try (java.sql.ResultSet resultSet = getAgtySQL()
                    .getConnection()
                    .getMetaData()
                    .getPrimaryKeys(null, null, tableName)) {
                while (resultSet.next()) {
                    return resultSet.getString("COLUMN_NAME");
                }
            }
        } catch (java.sql.SQLException e) {
            throw new org.agty.sql.exceptions.AgtySqlException("SQLite.getPrimaryKey()", e.getMessage(), e);
        }

        return null;
    }
}
