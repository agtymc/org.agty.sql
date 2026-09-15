package org.agty.sql.dialect.clickhouse;

import org.agty.sql.AgtySQL;
import org.agty.sql.data.Arguments;
import org.agty.sql.driver.DialectCapabilities;
import org.agty.sql.driver.DialectFeature;
import org.agty.sql.driver.DialectFeatureSet;
import org.agty.sql.driver.DialectFeatureSupport;
import org.agty.sql.driver.LastInsertIdStrategy;
import org.agty.sql.driver.UpdateAndGetStrategy;
import org.agty.sql.driver.WriteReturnStrategy;
import org.agty.sql.dialect.mysql.MySQL;
import org.agty.sql.exceptions.AgtySqlException;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.sqlbuilder.SqlValueRenderer;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Provides ClickHouse dialect behavior.
 */
public class ClickHouse extends MySQL {

    private static final String DRIVER = "clickhouse";
    private static final String DEFAULT_DATABASE = "default";

    /**
     * Creates a new instance.
     * @param agtySQL parameter value
     */
    public ClickHouse(AgtySQL agtySQL) {
        super(agtySQL);
    }

    @Override
    public String getDriverName() {
        return DRIVER;
    }

    @Override
    public String getDefaultDatabase() {
        return DEFAULT_DATABASE;
    }

    @Override
    public boolean isSupportLargeUpdate() {
        return true;
    }

    @Override
    public DialectCapabilities getCapabilities() {
        return DialectCapabilities.of(
                false,
                false,
                LastInsertIdStrategy.NONE,
                WriteReturnStrategy.NONE,
                UpdateAndGetStrategy.NONE
        );
    }

    @Override
    public DialectFeatureSet getFeatureSet() {
        return DialectFeatureSet.builder()
                .support(DialectFeature.JDBC_BATCH, DialectFeatureSupport.JDBC_DRIVER)
                .support(DialectFeature.POSITIONAL_PARAMETERS, DialectFeatureSupport.JDBC_DRIVER)
                .build();
    }

    @Override
    public String updateQuery(Arguments arguments) {
        if (!arguments.hasTable() || !arguments.hasData() || !arguments.hasWhere()) {
            return null;
        }

        return "ALTER TABLE "
                + arguments.getTable()
                + " UPDATE "
                + getMutationData(arguments)
                + " WHERE "
                + arguments.getWhere();
    }

    @Override
    public String deleteQuery(Arguments arguments) {
        if (!arguments.hasTable() || !arguments.hasWhere()) {
            return null;
        }

        return "ALTER TABLE "
                + arguments.getTable()
                + " DELETE WHERE "
                + arguments.getWhere();
    }

    @Override
    public boolean tableIsExists(String table) {
        SqlRow fetch = getAgtySQL().fetch(
                new Arguments()
                        .useStatementPrepare(true)
                        .setQuery(
                                "SELECT name FROM system.tables "
                                        + "WHERE database = currentDatabase() AND name = ? LIMIT 1",
                                table
                        )
                        .setNoRebuildQuery(true)
        );
        return fetch.isSet("name");
    }

    @Override
    public String truncateQuery(String table) {
        return "TRUNCATE TABLE " + table;
    }

    @Override
    public String getPrimaryKey(String table) {
        try {
            String tableName = getAgtySQL().rebuildTable(table);
            try (ResultSet resultSet = getAgtySQL()
                    .getConnection()
                    .getMetaData()
                    .getPrimaryKeys(null, getAgtySQL().getConfig().getDatabase(), tableName)) {
                while (resultSet.next()) {
                    return resultSet.getString("COLUMN_NAME");
                }
            }
        } catch (SQLException e) {
            throw new AgtySqlException("ClickHouse.getPrimaryKey()", e.getMessage(), e);
        }

        return null;
    }

    @Override
    public Long getLastInsertId(String table, String primaryKey) {
        return null;
    }

    private String getMutationData(Arguments arguments) {
        StringBuilder data = new StringBuilder();

        for (String key : arguments.getDataKeys()) {
            data.append(
                    new SqlValueRenderer()
                            .setQuoteColumn(getQuoteColumn())
                            .setQuoteValue(getQuoteValue())
                            .setColumn(key)
                            .setValue(arguments.getData(key))
                            .setNoStringEncode(arguments.noStringEncode())
                            .useStatementPrepare(arguments.useStatementPrepare())
                            .render()
            );
            data.append(",");
        }

        if (!data.isEmpty()) {
            data.setLength(data.length() - 1);
        }

        return data.isEmpty() ? null : data.toString();
    }
}
