package org.agty.sql.dialect.mssql;

import org.agty.sql.AgtySQL;
import org.agty.sql.data.Arguments;
import org.agty.sql.data.SqlExpression;
import org.agty.sql.driver.DialectCapabilities;
import org.agty.sql.driver.LastInsertIdStrategy;
import org.agty.sql.driver.UpdateAndGetStrategy;
import org.agty.sql.driver.WriteReturnStrategy;
import org.agty.sql.dialect.mysql.MySQL;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.support.PreparedStatementSupport;
import org.agty.sql.support.SqlTextUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class MsSQL extends MySQL {

    private static final String DRIVER = "sqlserver";
    private static final String DEFAULT_DATABASE = "master";
    private static final String QUOTE_IDENTIFIER = "\"";

    public MsSQL(AgtySQL agtySQL) {
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
    public String getQuoteTable() {
        return QUOTE_IDENTIFIER;
    }

    @Override
    public String getQuoteColumn() {
        return QUOTE_IDENTIFIER;
    }

    @Override
    public boolean isSupportLargeUpdate() {
        return true;
    }

    @Override
    public DialectCapabilities getCapabilities() {
        return DialectCapabilities.of(
                false,
                true,
                LastInsertIdStrategy.CONNECTION_FUNCTION,
                WriteReturnStrategy.NATIVE_RETURNING,
                UpdateAndGetStrategy.NATIVE_RETURNING
        );
    }

    @Override
    public String selectQuery(Arguments arguments) {
        return buildSelectQuery(arguments, false, false);
    }

    @Override
    public String insertQuery(Arguments arguments) {
        return "INSERT INTO "
                + arguments.getTable()
                + " ("
                + getInsertFields(arguments.getDataKeys())
                + ") VALUES ("
                + getInsertValues(
                        arguments.getDataValues(),
                        arguments.noStringEncode(),
                        arguments.useStatementPrepare()
                )
                + ")";
    }

    @Override
    public String updateQuery(Arguments arguments) {
        if (!arguments.hasTable()) {
            return null;
        }

        StringBuilder query = new StringBuilder();
        query.append("UPDATE ");
        query.append(arguments.getTable());
        query.append(" SET ");
        query.append(getUpdateData(arguments));

        if (arguments.hasWhere()) {
            query.append(" WHERE ");
            query.append(arguments.getWhere());
        }

        return query.toString();
    }

    @Override
    public String fetchQuery(Arguments arguments) {
        return buildSelectQuery(arguments, true, false);
    }

    @Override
    public String getFirstRowQuery(Arguments arguments) {
        return buildSelectQuery(arguments, true, false, " ASC");
    }

    @Override
    public String getLastRowQuery(Arguments arguments) {
        return buildSelectQuery(arguments, true, true, " DESC");
    }

    @Override
    public ResultSet insertAndGet(Arguments arguments, String fields) {
        String query = arguments.getQuery() != null && arguments.getQuery().length() >= 3
                ? arguments.getQuery()
                : insertQuery(arguments);
        String returningQuery = insertOutputClause(query, fields);
        if (arguments.useStatementPrepare()) {
            return PreparedStatementSupport.executeQuery(
                    getAgtySQL(),
                    returningQuery,
                    PreparedStatementSupport.insertParameters(arguments),
                    arguments.noRebuildQuery(),
                    "MsSQL.insertAndGet()"
            );
        }
        return getAgtySQL().executeResultSet(returningQuery, arguments.noRebuildQuery());
    }

    @Override
    public ResultSet updateAndGet(Arguments arguments, String fields) {
        String query = arguments.getQuery() != null && arguments.getQuery().length() >= 3
                ? arguments.getQuery()
                : updateQuery(arguments);
        String returningQuery = updateOutputClause(query, fields);
        if (arguments.useStatementPrepare()) {
            return PreparedStatementSupport.executeQuery(
                    getAgtySQL(),
                    returningQuery,
                    PreparedStatementSupport.updateParameters(arguments),
                    arguments.noRebuildQuery(),
                    "MsSQL.updateAndGet()"
            );
        }
        return getAgtySQL().executeResultSet(returningQuery, arguments.noRebuildQuery());
    }

    @Override
    public boolean tableIsExists(String table) {
        String normalizedSchema = normalizeSchema();
        String normalizedTable = getAgtySQL().rebuildTable(table);
        SqlRow fetch = getAgtySQL().fetch(
                new Arguments()
                        .useStatementPrepare(true)
                        .setQuery(
                                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES "
                                        + "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?",
                                normalizedSchema,
                                normalizedTable
                        )
                        .setNoRebuildQuery(true)
        );
        return fetch.isSet("TABLE_NAME");
    }

    @Override
    public Boolean rowIsExists(Arguments arguments) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT CASE WHEN EXISTS (");

        if (arguments.hasQuery()) {
            query.append(arguments.getQuery());
        } else {
            query.append("SELECT ");
            query.append(arguments.getFields());
            query.append(" FROM ");
            query.append(arguments.getTable());

            if (arguments.hasWhere()) {
                query.append(" WHERE ");
                query.append(arguments.getWhere());
            }
        }

        query.append(") THEN 1 ELSE 0 END AS is_exists");

        SqlRow row = getAgtySQL().fetch(
                PreparedStatementSupport.readQueryArguments(arguments, query.toString())
        );

        return row.getInt("is_exists") == 1;
    }

    @Override
    public String truncateQuery(String table) {
        return "TRUNCATE TABLE " + rebuildQualifiedTable(table);
    }

    @Override
    public String dropTableQuery(String table) {
        return "DROP TABLE IF EXISTS " + rebuildQualifiedTable(table);
    }

    @Override
    public String dropColumnQuery(Arguments arguments) {
        StringBuilder query = new StringBuilder();

        if (arguments.hasColumns()) {
            query.append("ALTER TABLE ");
            query.append(rebuildQualifiedTable(arguments.getTable()));

            for (String column : arguments.getColumns()) {
                query.append(" DROP COLUMN ");
                query.append(getQuoteColumn());
                query.append(column);
                query.append(getQuoteColumn());
                query.append(",");
            }

            query.setLength(query.length() - 1);
        }

        return query.toString();
    }

    @Override
    public Long getLastInsertId(String table, String primaryKey) {
        SqlRow row = getAgtySQL().fetch(
                new Arguments()
                        .setQuery(SqlExpression.trusted(
                                "SELECT CAST(SCOPE_IDENTITY() AS BIGINT) AS last_id"
                        ))
                        .setNoRebuildQuery(true)
        );
        return row.getLong("last_id");
    }

    @Override
    public String getPrimaryKey(String table) {
        try {
            String tableName = getAgtySQL().rebuildTable(table);
            String schema = normalizeSchema();

            try (ResultSet resultSet = getAgtySQL()
                    .getConnection()
                    .getMetaData()
                    .getPrimaryKeys(getAgtySQL().getConfig().getDatabase(), schema, tableName)) {
                while (resultSet.next()) {
                    return resultSet.getString("COLUMN_NAME");
                }
            }

            try (ResultSet resultSet = getAgtySQL()
                    .getConnection()
                    .getMetaData()
                    .getPrimaryKeys(getAgtySQL().getConfig().getDatabase(), schema.toUpperCase(Locale.ROOT), tableName)) {
                while (resultSet.next()) {
                    return resultSet.getString("COLUMN_NAME");
                }
            }
        } catch (SQLException e) {
            throw new org.agty.sql.exceptions.AgtySqlException("MsSQL.getPrimaryKey()", e.getMessage(), e);
        }

        return null;
    }

    private String buildSelectQuery(Arguments arguments, boolean singleRow, boolean forceActionFieldOrder) {
        return buildSelectQuery(arguments, singleRow, forceActionFieldOrder, null);
    }

    private String buildSelectQuery(
            Arguments arguments,
            boolean singleRow,
            boolean forceActionFieldOrder,
            String actionFieldOrderSuffix
    ) {
        if (!arguments.hasTable()) {
            return null;
        }

        StringBuilder query = new StringBuilder("SELECT ");

        if (singleRow) {
            query.append("TOP 1 ");
        }

        query.append(arguments.getFields());
        query.append(" FROM ");
        query.append(arguments.getTable());

        if (arguments.hasWhere()) {
            query.append(" WHERE ");
            query.append(arguments.getWhere());
        }
        if (arguments.hasGroupBy()) {
            query.append(" GROUP BY ");
            query.append(arguments.getGroupBy());
        }
        if (arguments.hasHaving()) {
            query.append(" HAVING ");
            query.append(arguments.getHaving());
        }

        if (forceActionFieldOrder && arguments.hasActionField()) {
            query.append(" ORDER BY ");
            query.append(arguments.getActionField());
            query.append(actionFieldOrderSuffix);
        } else if (arguments.hasOrderBy()) {
            query.append(" ORDER BY ");
            query.append(arguments.getOrderBy());
        }

        if (!singleRow && arguments.hasLimit()) {
            if (!arguments.hasOrderBy()) {
                query.append(" ORDER BY (SELECT 1)");
            }

            query.append(" OFFSET ");
            query.append(Math.max(arguments.getOffset(), 0));
            query.append(" ROWS FETCH NEXT ");
            query.append(arguments.getLimit());
            query.append(" ROWS ONLY");
        }

        return query.toString();
    }

    private String getInsertFields(List<String> keysArray) {
        StringBuilder fields = new StringBuilder();

        for (String key : keysArray) {
            fields.append(getQuoteColumn());
            fields.append(key);
            fields.append(getQuoteColumn());
            fields.append(",");
        }

        if (!fields.isEmpty()) {
            fields.setLength(fields.length() - 1);
        }

        return fields.isEmpty() ? null : fields.toString();
    }

    private String getInsertValues(
            List<Object> valuesArray,
            boolean noStringEncode,
            boolean statementPrepare
    ) {
        StringBuilder values = new StringBuilder();

        for (Object value : valuesArray) {
            values.append(renderValue(value, noStringEncode, statementPrepare));
            values.append(",");
        }

        if (!values.isEmpty()) {
            values.setLength(values.length() - 1);
        } else {
            return null;
        }

        return values.toString();
    }

    private String getUpdateData(Arguments arguments) {
        StringBuilder updateData = new StringBuilder();

        for (String key : arguments.getDataKeys()) {
            updateData.append(getQuoteColumn());
            updateData.append(key);
            updateData.append(getQuoteColumn());
            updateData.append('=');
            updateData.append(renderValue(
                    arguments.getData(key),
                    arguments.noStringEncode(),
                    arguments.useStatementPrepare()
            ));
            updateData.append(",");
        }

        if (!updateData.isEmpty()) {
            updateData.setLength(updateData.length() - 1);
        } else {
            return null;
        }

        return updateData.toString();
    }

    private String renderValue(Object value, boolean noStringEncode, boolean statementPrepare) {
        if (statementPrepare) {
            return "?";
        }
        if (value == null || value.toString().isEmpty()) {
            return "NULL";
        }
        if (value instanceof Number) {
            return value.toString();
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue ? "1" : "0";
        }
        if (value.toString().startsWith("[~")) {
            return encodeValue(value.toString(), noStringEncode).substring(2);
        }
        return getQuoteValue() + encodeValue(value.toString(), noStringEncode) + getQuoteValue();
    }

    private String encodeValue(String value, boolean noStringEncode) {
        return noStringEncode ? value : SqlTextUtils.hencode(value);
    }

    private String insertOutputClause(String query, String fields) {
        int valuesIndex = indexOfKeyword(query, "VALUES");
        if (valuesIndex < 0) {
            return query + " OUTPUT " + qualifyInsertedFields(fields);
        }

        return query.substring(0, valuesIndex)
                + "OUTPUT "
                + qualifyInsertedFields(fields)
                + " "
                + query.substring(valuesIndex);
    }

    private String updateOutputClause(String query, String fields) {
        int whereIndex = indexOfKeyword(query, "WHERE");
        if (whereIndex < 0) {
            return query + " OUTPUT " + qualifyInsertedFields(fields);
        }

        return query.substring(0, whereIndex)
                + "OUTPUT "
                + qualifyInsertedFields(fields)
                + " "
                + query.substring(whereIndex);
    }

    private String qualifyInsertedFields(String fields) {
        if (fields == null || fields.isBlank()) {
            return "inserted.*";
        }

        String normalized = fields.trim();
        if ("*".equals(normalized)) {
            return "inserted.*";
        }

        String[] parts = normalized.split(",");
        StringBuilder qualified = new StringBuilder();
        for (String part : parts) {
            String field = part.trim();
            if (field.isEmpty()) {
                continue;
            }
            if (field.contains(".") || field.toLowerCase(Locale.ROOT).startsWith("inserted ")) {
                qualified.append(field);
            } else {
                qualified.append("inserted.").append(field);
            }
            qualified.append(", ");
        }

        if (!qualified.isEmpty()) {
            qualified.setLength(qualified.length() - 2);
        }

        return qualified.isEmpty() ? "inserted.*" : qualified.toString();
    }

    private int indexOfKeyword(String query, String keyword) {
        return query.toUpperCase(Locale.ROOT).indexOf(" " + keyword + " ");
    }

    private String rebuildQualifiedTable(String table) {
        String rebuiltTable = getAgtySQL().rebuildTable(table);
        if (rebuiltTable.contains(".")) {
            return rebuiltTable;
        }
        return normalizeSchema() + "." + rebuiltTable;
    }

    private String normalizeSchema() {
        String schema = getAgtySQL().getConfig().getSchema();
        return schema == null || schema.isBlank() ? "dbo" : schema;
    }
}
