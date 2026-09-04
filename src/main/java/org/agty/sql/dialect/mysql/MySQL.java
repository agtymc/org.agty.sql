package org.agty.sql.dialect.mysql;

import org.agty.sql.dialect.mysql.queries.*;
import org.agty.sql.data.Arguments;
import org.agty.sql.data.InsertData;
import org.agty.sql.data.SqlExpression;
import org.agty.sql.driver.DialectCapabilities;
import org.agty.sql.driver.LastInsertIdStrategy;
import org.agty.sql.driver.UpdateAndGetStrategy;
import org.agty.sql.driver.WriteReturnStrategy;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.AgtySQL;
import org.agty.sql.interfaces.Sql;
import org.agty.sql.sqlbuilder.SqlValueRenderer;
import org.agty.sql.support.PreparedStatementSupport;
import org.agty.sql.support.SqlIdentifierValidator;

import java.sql.ResultSet;
import java.util.*;

/**
 * MySQL driver
 */
public class MySQL implements Sql {
    private final AgtySQL agtySQL;
    private static final String DRIVER = "mysql";
    private static final String DEFAULT_DATABASE = "";
    private static final String QUOTE_TABLE = "`";
    private static final String QUOTE_COLUMN = "`";
    private static final String QUOTE_VALUE = "'";
    private static final boolean SUPPORT_LARGE_UPDATE = false;

    /**
     * Constructor.
     * Принимает в себя класс AgtySQL;
     *
     * @param agtySQL объект AgtySQL
     */
    public MySQL(AgtySQL agtySQL) {
        this.agtySQL = agtySQL;
    }

    /**
     * AgtySQL object.
     * @return объект AgtySQL.
     */
    protected AgtySQL getAgtySQL() {
        return agtySQL;
    }

    /**
     * Quotes for a table.
     * @return кавычки.
     */
    @Override
    public String getQuoteTable() {
        return QUOTE_TABLE;
    }

    /**
     * Quotes for a field.
     * @return кавычки.
     */
    @Override
    public String getQuoteColumn() {
        return QUOTE_COLUMN;
    }

    /**
     * Quotes for a value.
     * @return кавычки.
     */
    @Override
    public String getQuoteValue() {
        return QUOTE_VALUE;
    }

    /**
     * Full driver name.
     * @return кавычки.
     */
    @Override
    public String getDriverName() {
        return DRIVER;
    }

    /**
     * Default database.
     * @return имя базы данных.
     */
    @Override
    public String getDefaultDatabase() {
        return DEFAULT_DATABASE;
    }

    /**
     * Support large update.
     * @return кавычки.
     */
    @Override
    public boolean isSupportLargeUpdate() {
        return SUPPORT_LARGE_UPDATE;
    }

    @Override
    public DialectCapabilities getCapabilities() {
        return DialectCapabilities.of(
                false,
                false,
                LastInsertIdStrategy.CONNECTION_FUNCTION,
                WriteReturnStrategy.FOLLOW_UP_FETCH,
                UpdateAndGetStrategy.FOLLOW_UP_FETCH_BY_WHERE
        );
    }

    /**
     * A select query.
     *
     * @param arguments объект Arguments.
     * @return строка запроса.
     */
    @Override
    public String selectQuery(Arguments arguments) {
        return new QuerySelect(arguments).getQuery();
    }
    
    /**
     * Insert data into a table.
     *
     * @param arguments аргументы запроса.
     * @return String строка запроса
     */
    @Override
    public String insertQuery(Arguments arguments) {
        return createInsertQuery(getInsertData(arguments));
    }

    /**
     * Insert an array data into a table.
     *
     * @param arguments Params
     * @return String
     */
    @Override
    public String insertQuery(ArrayList<Arguments> arguments) {
        return createInsertQuery(getInsertData(arguments));
    }

    /**
     * Return PRIMARY KEY name by a table name.
     *
     * @param table имя таблицы.
     * @return основной ключ.
     */
    @Override
    public String getPrimaryKey(String table) {
        try {
            String tableName = getAgtySQL().rebuildTable(table);
            try (java.sql.ResultSet resultSet = getAgtySQL()
                    .getConnection()
                    .getMetaData()
                    .getPrimaryKeys(getAgtySQL().getConfig().getDatabase(), null, tableName)) {
                while (resultSet.next()) {
                    return resultSet.getString("COLUMN_NAME");
                }
            }

            try (java.sql.ResultSet resultSet = getAgtySQL()
                    .getConnection()
                    .getMetaData()
                    .getPrimaryKeys(null, null, tableName)) {
                while (resultSet.next()) {
                    return resultSet.getString("COLUMN_NAME");
                }
            }
        } catch (java.sql.SQLException e) {
            throw new org.agty.sql.exceptions.AgtySqlException("MySQL.getPrimaryKey()", e.getMessage(), e);
        }

        return null;
    }

    /**
     * Return PRIMARY KEY name by an Arguments.getPrimaryKey() or Arguments.getTable().
     *
     * @param arguments объект Arguments.
     * @return основной ключ.
     */
    @Override
    public String getPrimaryKey(Arguments arguments) {
        if (arguments.getPrimaryKey() != null) {
            return arguments.getPrimaryKey();
        }
        return getPrimaryKey(arguments.getTable());
    }

    /**
     * Insert a row and get a result
     *
     * @param arguments Arguments
     * @return ResultSet
     */
    @Override
    public ResultSet insertAndGet(Arguments arguments, String fields) {
        String query = arguments.getQuery() != null && arguments.getQuery().length() >= 3 ? arguments.getQuery() : insertQuery(arguments);
        return getAgtySQL().executeResultSet(query + " RETURNING " + fields, arguments.noRebuildQuery());
    }

    /**
     * Update a row and get a result
     *
     * @param arguments Arguments
     * @param fields
     * @return SqlRow
     */
    @Override
    public ResultSet updateAndGet(Arguments arguments, String fields) {
        getAgtySQL().update(arguments);

        String query = arguments.getQuery() != null && arguments.getQuery().length() >= 3
                ? arguments.getQuery()
                : fetchQuery(arguments);

        return getAgtySQL().executeQuery(query, arguments.noRebuildQuery());
    }

    /**
     * Fetch a row.
     *
     * @param arguments объект Arguments.
     * @return строка запроса.
     */
    @Override
    public String fetchQuery(Arguments arguments) {
        return new QueryFetch(arguments).getQuery();
    }

    /**
     * An update query.
     *
     * @param arguments объект Arguments.
     * @return строка запроса.
     */
    @Override
    public String updateQuery(Arguments arguments) {
        QueryUpdate QueryUpdate = new QueryUpdate(arguments)
                .setQuoteTable(getQuoteTable())
                .setQuoteColumn(getQuoteColumn())
                .setQuoteValue(getQuoteValue())
                .setUpdateData(getUpdateData(arguments));

        if (arguments.hasGroupBy()) {
            QueryUpdate.setPrimaryKey(getPrimaryKey(arguments));
        }

        return QueryUpdate.getQuery();
    }

    /**
     * Запрос удаления строк.
     *
     * @param arguments объект Arguments
     * @return строка запроса.
     */
    @Override
    public String deleteQuery(Arguments arguments) {
        QueryDelete QueryDelete = new QueryDelete(arguments);

        QueryDelete.setQuoteTable(getQuoteTable())
                .setQuoteColumn(getQuoteColumn())
                .setQuoteValue(getQuoteValue());

        if (arguments.hasGroupBy()) {
            QueryDelete.setPrimaryKey(getPrimaryKey(arguments));
        }

        return QueryDelete.getQuery();

    }

    /**
     * Запрос подсчета количества строк
     *
     * @param arguments Params
     * @return String
     */
    @Override
    public String countRowsQuery(Arguments arguments) {
        return new QueryCountRows(arguments)
                .setQuoteTable(getQuoteTable())
                .setQuoteColumn(getQuoteColumn())
                .setQuoteValue(getQuoteValue())
                .getQuery();
    }

    /**
     * Проверка наличие таблицы.
     *
     * @param table имя таблицы
     * @return true если таблица есть в базе
     */
    @Override
    public boolean tableIsExists(String table) {
        String database = SqlIdentifierValidator.requireColumn(
                getAgtySQL().getConfig().getDatabase(),
                "database"
        );
        SqlRow fetch = getAgtySQL().fetch(
                new Arguments()
                        .useStatementPrepare(true)
                        .setQuery("SHOW TABLE STATUS FROM `" + database + "` LIKE ?", table)
                        .setNoRebuildQuery(true)
        );
        return fetch.isSet("TABLE_NAME") || fetch.isSet("Name");
    }

    /**
     * Запрос проверки наличие строк в таблице
     *
     * @param arguments объект Arguments.
     * @return true если строки найдены, null если ошибка в запросе.
     */
    @Override
    public Boolean rowIsExists(Arguments arguments) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT EXISTS (");

        if (arguments.hasQuery()) {
            query.append(arguments.getQuery());
        }
        else {
            query.append("SELECT ");
            query.append(arguments.getFields());
            query.append(" FROM ");
            query.append(arguments.getTable());

            if (arguments.hasWhere()) {
                query.append(" WHERE ");
                query.append(arguments.getWhere());
            }
        }

        query.append(") AS is_exists");

        SqlRow getData = getAgtySQL().fetch(
                PreparedStatementSupport.readQueryArguments(arguments, query.toString())
        );

        if (!getData.isSet("is_exists")) {
            return false;
        }

        Object existsValue = getData.getObject("is_exists");
        if (existsValue instanceof Boolean booleanValue) {
            return booleanValue;
        }

        return getData.getInt("is_exists") == 1;
    }

    /**
     * Очищение таблицу и обнуление счетчика последовательностей.
     *
     * @param table имя таблицы.
     * @return true, если нет ошибок.
     */
    @Override
    public String truncateQuery(String table) {
        return "TRUNCATE TABLE `" + getAgtySQL().getConfig().getDatabase() + "`.`" + table + "`";
    }

    /**
     * Обнулить все последовательности.
     *
     * @param table имя таблицы.
     */
    @Override
    public boolean restartIdentity(String table) {
        return true;
    }

    /**
     * Запрос удаления столбца таблицы
     *
     * @param arguments объект Arguments.
     * @return String
     */
    @Override
    public String dropColumnQuery(Arguments arguments) {
        StringBuilder query = new StringBuilder();

        if (arguments.hasColumns()) {
            query.append("ALTER TABLE ");
            query.append(arguments.getTable());

            for (String column : arguments.getColumns()) {
                query.append(" DROP COLUMN IF EXISTS ");
                query.append(column);
                query.append(",");
            }

            query.setLength(query.length() - 1);
        }

        return query.toString();
    }

    /**
     * Запрос удаления таблицы
     *
     * @param table имя таблицы.
     * @return String
     */
    @Override
    public String dropTableQuery(String table) {
        return "DROP TABLE IF EXISTS `" + table + "`";
    }

    /**
     * Последний вставленный ID в таблице
     *
     * @param table имя таблицы.
     * @return int последний вставленный id.
     */
    @Override
    public Long getLastInsertId(String table) {
        return getLastInsertId(table, null);
    }

    /**
     * Инициализация запроса на получение последнего вставленного ID в таблице
     *
     * @param table имя таблицы.
     * @param primaryKey основной ключ.
     * @return Integer последний вставленный id.
     */
    @Override
    public Long getLastInsertId(String table, String primaryKey) {
        SqlRow fetchLastId = getAgtySQL().fetch(
                new Arguments()
                        .setQuery(SqlExpression.trusted("SELECT LAST_INSERT_ID() AS last_id"))
                        .setNoRebuildQuery(true)
        );

        return fetchLastId.getLong("last_id");
    }

    /**
     * Максимальное значение по указанному полю.
     *
     * @param arguments объект Arguments.
     * @return int|null.
     */
    public Long max(Arguments arguments) {

        String query = new QueryMinMax(arguments).getMax().getQuery();

        if (query == null) return null;

        SqlRow getData = getAgtySQL().fetch(
                PreparedStatementSupport.readQueryArguments(arguments, query)
        );

        return getData.getLong("M");
    }

    /**
     * Минимальное значение по указанному полю.
     *
     * @param arguments объект Arguments.
     * @return int|null.
     */
    public Long min(Arguments arguments) {

        String query = new QueryMinMax(arguments).getMin().getQuery();

        if (query == null) return null;

        SqlRow getData = getAgtySQL().fetch(
                PreparedStatementSupport.readQueryArguments(arguments, query)
        );

        return getData.getLong("M");
    }

    /**
     * Получить последнюю строку.
     *
     * @param arguments объект Arguments.
     * @return строка запроса.
     */
    public String getLastRowQuery(Arguments arguments) {
        return new QueryFirstLast(arguments).getLast().getQuery();
    }

    /**
     * Получить первую строку.
     *
     * @param arguments объект Arguments.
     * @return строка запроса.
     */
    public String getFirstRowQuery(Arguments arguments) {
        return new QueryFirstLast(arguments).getFirst().getQuery();
    }

    /**
     * DONE. Строка запроса Insert.
     *
     * @param insertData объект с параметрами.
     * @return строка запроса.
     */
    private String createInsertQuery(InsertData insertData) {
        return new QueryInsert(insertData.getArguments()).setInsertData(insertData).getQuery();
    }

    /**
     * DONE. Преобразует массив данных insert в массив (одна строка)
     *
     * @return array|null|string
     */
    private InsertData getInsertData(Arguments arguments) {

        InsertData insertData = new InsertData();
        insertData.setArguments(arguments);
        insertData.setFields(getInsertFields(arguments.getDataKeys()));
        insertData.setValue(getInsertValues(
                arguments.getDataValues(),
                arguments.noStringEncode(),
                arguments.useStatementPrepare()
        ));

        return insertData;
    }

    /**
     * CHECK. Преобразует массив данных insert в массив (множество строк)
     *
     * @return array|null|string
     */
    private InsertData getInsertData(ArrayList<Arguments> argumentsArray) {
        InsertData insertData = new InsertData();
        insertData.setArguments(argumentsArray.get(0));
        insertData.setFields(getInsertFields(argumentsArray.get(0).getDataKeys()));

        for (Arguments arguments : argumentsArray) {
            insertData.setValue(
                    getInsertValues(
                            arguments.getDataValues(),
                            arguments.noStringEncode(),
                            arguments.useStatementPrepare()
                    )
            );
        }

        return insertData;
    }

    /**
     * DONE. Список полей через запятую с кавычками из массива полей.
     * Возвращает: "fieldName", "fieldName2", ...
     *
     * @param keysArray массив полей.
     * @return строка параметров.
     */
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

    /**
     * Список значений через запятую с кавычками из массива значений.
     * Возвращает: 'value1', "value2', ...
     *
     * @param valuesArray массив полей.
     * @param noStringEncode если true, тогда спецсимволы не будут преобразованы.
     * @return строка параметров.
     */
    private String getInsertValues(
            List<Object> valuesArray,
            boolean noStringEncode,
            boolean statementPrepare
    ) {

        StringBuilder values = new StringBuilder();

        //'value'
        for (Object value : valuesArray) {
            values.append(
                    new SqlValueRenderer()
                            .setQuoteColumn(getQuoteColumn())
                            .setQuoteValue(getQuoteValue())
                            .setValue(value)
                            .setNoStringEncode(noStringEncode)
                            .useStatementPrepare(statementPrepare)
                            .render()
            );

            values.append(",");
        }

        if (!values.isEmpty()) {
            values.setLength(values.length() - 1);
        } else {
            return null;
        }

        return values.toString();
    }

    /**
     * Строка обновления данных.
     * Возвращает: "key"='value1', "key2"='value2', ...
     *
     * @param arguments объект Arguments
     * @return строка параметров.
     */
    private String getUpdateData(Arguments arguments) {
        StringBuilder returnData = new StringBuilder();

        for (String key : arguments.getDataKeys()) {

            //"key"=value
            returnData.append(
                    new SqlValueRenderer()
                            .setQuoteColumn(getQuoteColumn())
                            .setQuoteValue(getQuoteValue())
                            .setColumn(key)
                            .setValue(arguments.getData(key))
                            .setNoStringEncode(arguments.noStringEncode())
                            .useStatementPrepare(arguments.useStatementPrepare())
                            .render()
            );

            returnData.append(",");
        }

        if (!returnData.isEmpty()) {
            returnData.setLength(returnData.length() - 1);
        } else {
            return null;
        }

        return returnData.toString();
    }
}
