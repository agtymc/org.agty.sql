package org.agty.sql.dialect.h2;

import org.agty.sql.AgtySQL;
import org.agty.sql.data.Arguments;
import org.agty.sql.data.SqlExpression;
import org.agty.sql.driver.DialectCapabilities;
import org.agty.sql.driver.LastInsertIdStrategy;
import org.agty.sql.driver.UpdateAndGetStrategy;
import org.agty.sql.driver.WriteReturnStrategy;
import org.agty.sql.dialect.mysql.MySQL;
import org.agty.sql.interfaces.SqlRow;

import java.util.Locale;

public class H2 extends MySQL {

    private static final String DRIVER = "h2";

    public H2(AgtySQL agtySQL) {
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
                true,
                LastInsertIdStrategy.FETCH_LAST_ROW_UNSAFE,
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
                                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = UPPER(?) LIMIT 1",
                                table
                        )
                        .setNoRebuildQuery(true)
        );
        return fetch.isSet("TABLE_NAME");
    }

    @Override
    public Long getLastInsertId(String table, String primaryKey) {
        SqlRow row = getAgtySQL().fetch(
                new Arguments()
                        .setQuery(SqlExpression.trusted("CALL IDENTITY()"))
                        .setNoRebuildQuery(true)
        );
        return row.getLong("IDENTITY()");
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

            try (java.sql.ResultSet resultSet = getAgtySQL()
                    .getConnection()
                    .getMetaData()
                    .getPrimaryKeys(null, null, tableName.toUpperCase(Locale.ROOT))) {
                while (resultSet.next()) {
                    return resultSet.getString("COLUMN_NAME");
                }
            }
        } catch (java.sql.SQLException e) {
            throw new org.agty.sql.exceptions.AgtySqlException("H2.getPrimaryKey()", e.getMessage(), e);
        }

        return null;
    }
}
