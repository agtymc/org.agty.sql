package org.agty.sql.dialect.pgsql;

import org.agty.sql.dialect.pgsql.queries.*;
import org.agty.sql.data.Arguments;
import org.agty.sql.data.InsertData;
import org.agty.sql.driver.DialectCapabilities;
import org.agty.sql.driver.LastInsertIdStrategy;
import org.agty.sql.driver.UpdateAndGetStrategy;
import org.agty.sql.driver.WriteReturnStrategy;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.AgtySQL;
import org.agty.sql.interfaces.Sql;
import org.agty.sql.sqlbuilder.QuerySelectBuilder;
import org.agty.sql.sqlbuilder.SqlValueRenderer;
import org.agty.sql.support.PreparedStatementSupport;

import java.sql.ResultSet;
import java.util.*;

/**
 * PgSQL driver
 */
public class PgSQL implements Sql {
    private final AgtySQL agtySQL;
    private static final String DRIVER = "postgresql";
    private static final String DEFAULT_DATABASE = "template1";
    private static final String QUOTE_TABLE = "\"";
    private static final String QUOTE_COLUMN = "\"";
    private static final String QUOTE_VALUE = "'";
    private static final boolean SUPPORT_LARGE_UPDATE = true;

    /**
     * Constructor.
     * @param agtySQL AgtySQL object
     */
    public PgSQL(AgtySQL agtySQL) {
        this.agtySQL = agtySQL;
    }

    /**
     * AgtySQL object.
     * @return объект AgtySQL.
     */
    private AgtySQL getAgtySQL() {
        return agtySQL;
    }

    /**
     * Quotes for a table.
     * @return quotes
     */
    @Override
    public String getQuoteTable() {
        return QUOTE_TABLE;
    }

    /**
     * Quotes for a field.
     * @return quotes
     */
    @Override
    public String getQuoteColumn() {
        return QUOTE_COLUMN;
    }

    /**
     * Quotes for a value.
     * @return quotes
     */
    @Override
    public String getQuoteValue() {
        return QUOTE_VALUE;
    }

    /**
     * Full driver name.
     * @return quotes
     */
    @Override
    public String getDriverName() {
        return DRIVER;
    }

    /**
     * Default database.
     * @return a database
     */
    @Override
    public String getDefaultDatabase() {
        return DEFAULT_DATABASE;
    }

    /**
     * Support large update.
     * @return bool
     */
    @Override
    public boolean isSupportLargeUpdate() {
        return SUPPORT_LARGE_UPDATE;
    }

    @Override
    public DialectCapabilities getCapabilities() {
        return DialectCapabilities.of(
                false,
                true,
                LastInsertIdStrategy.SEQUENCE_FUNCTION,
                WriteReturnStrategy.NATIVE_RETURNING,
                UpdateAndGetStrategy.NATIVE_RETURNING
        );
    }

    /**
     * A select query.
     *
     * @param arguments rguments.
     * @return a string query
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
        SqlRow fetch = getAgtySQL().fetch(
                "SELECT a.attname as \"primaryKey\" FROM pg_index i JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY(i.indkey) WHERE  i.indrelid = '\"" + getAgtySQL().getConfig().getSchema() + "\".\"" + getAgtySQL().rebuildTable( table ) + "\"'::regclass AND i.indisprimary"
        );
        return fetch.getString("primaryKey");
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
        QueryUpdate queryUpdate = new QueryUpdate(arguments)
                .setQuoteTable(getQuoteTable())
                .setQuoteColumn(getQuoteColumn())
                .setQuoteValue(getQuoteValue())
                .setUpdateData(getUpdateData(arguments));

        if (arguments.hasOneOfOrderGroupHaving() || arguments.hasLimitOrOffset()) {
            queryUpdate.setPrimaryKey(getPrimaryKey(arguments));
        }

        return queryUpdate.getQuery();
    }

    /**
     * Запрос удаления строк.
     *
     * @param arguments объект Arguments
     * @return строка запроса.
     */
    @Override
    public String deleteQuery(Arguments arguments) {
        QueryDelete queryDelete = new QueryDelete(arguments);

        queryDelete.setQuoteTable(getQuoteTable())
                .setQuoteColumn(getQuoteColumn())
                .setQuoteValue(getQuoteValue());

        if (arguments.hasOneOfOrderGroupHaving() || arguments.hasLimitOrOffset()) {
            queryDelete.setPrimaryKey(getPrimaryKey(arguments));
        }

        return queryDelete.getQuery();

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
        SqlRow fetch = getAgtySQL().fetch(
                "SELECT tablename FROM pg_catalog.pg_tables WHERE tablename = '" + table + "' LIMIT 1;"
        );
        return fetch.isSet("tablename");
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
            query.append(
              new QuerySelectBuilder()
                      .addField(arguments.getFields())
                      .setTable(arguments.getTable())
                      .setWhere(arguments.getWhere())
                      .build()
            );
        }

        query.append(") AS is_exists");

        SqlRow getData = getAgtySQL().fetch(
                PreparedStatementSupport.readQueryArguments(arguments, query.toString())
        );

        return getData.getBoolean("is_exists");
    }

    /**
     * Очищение таблицу и обнуление счетчика последовательностей.
     *
     * @param table имя таблицы.
     * @return true, если нет ошибок.
     */
    @Override
    public String truncateQuery(String table) {
        return "TRUNCATE TABLE \"" + table + "\" RESTART IDENTITY CASCADE";
    }

    /**
     * NO NEED. Обнулить все последовательности.
     *
     * @param table имя таблицы.
     */
    @Override
    public boolean restartIdentity(String table) {
        String sequenceName = getSequenceName(table, getPrimaryKey(table));

        if (sequenceName != null) {
            getAgtySQL().execute( "SELECT setval('" + sequenceName + "', 1, false);", true);
        }

        return !getAgtySQL().hasErrors();
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
        return "DROP TABLE IF EXISTS \"" + table + "\"";
    }

    /**
     * Последний вставленный ID в таблице
     *
     * @param table имя таблицы.
     * @return int последний вставленный id.
     */
    @Override
    public Long getLastInsertId(String table) {
        return getLastInsertId(
                table,
                getPrimaryKey(table)
        );
    }

    /**
     * Инициализация запроса на получение последнего вставленного ID в таблице
     *
     * @param table имя таблицы.
     * @param primaryKey основной ключ.
     * @return Long последний вставленный id.
     */
    @Override
    public Long getLastInsertId(String table, String primaryKey) {

        String sequenceName = getSequenceName(table, primaryKey);

        if (sequenceName == null || sequenceName.isEmpty()) return null;

        SqlRow fetchLastId = getAgtySQL().fetch(
           "SELECT currval('" + sequenceName + "') as \"lastId\""
        );

        return fetchLastId.getLong("lastId");
    }

    /**
     * Имя последовательности.
     *
     * @param table имя таблицы.
     * @param field имя поля.
     * @return имя последовательности.
     */
    private String getSequenceName(String table, String field) {
        SqlRow fetchSeqName = getAgtySQL().fetch(
                "SELECT pg_get_serial_sequence('\"" + getAgtySQL().getConfig().getSchema() + "\".\"" + table + "\"', '" + field + "') as seq_name"
        );

        return fetchSeqName.getString("seq_name");
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
     * Insert a row and get a result
     *
     * @param arguments Arguments
     * @return ResultSet
     */
    @Override
    public ResultSet insertAndGet(Arguments arguments, String fields) {
        String query = arguments.getQuery() != null && arguments.getQuery().length() >= 3 ? arguments.getQuery() : insertQuery(arguments);
        if (arguments.useStatementPrepare()) {
            return PreparedStatementSupport.executeQuery(
                    getAgtySQL(),
                    query + " RETURNING " + fields,
                    PreparedStatementSupport.insertParameters(arguments),
                    arguments.noRebuildQuery(),
                    "PgSQL.insertAndGet()"
            );
        }
        return getAgtySQL().executeResultSet(query + " RETURNING " + fields, arguments.noRebuildQuery());
    }

    /**
     * Update a row and get a result
     *
     * @param arguments Arguments
     * @param fields
     * @return ResultSet
     */
    @Override
    public ResultSet updateAndGet(Arguments arguments, String fields) {
        String query = arguments.getQuery() != null && arguments.getQuery().length() >= 3 ? arguments.getQuery() : updateQuery(arguments);
        if (arguments.useStatementPrepare()) {
            return PreparedStatementSupport.executeQuery(
                    getAgtySQL(),
                    query + " RETURNING " + fields,
                    PreparedStatementSupport.updateParameters(arguments),
                    arguments.noRebuildQuery(),
                    "PgSQL.updateAndGet()"
            );
        }
        return getAgtySQL().executeResultSet(query + " RETURNING " + fields, arguments.noRebuildQuery());
    }

    /**
     * Строка запроса Insert.
     *
     * @param insertData объект с параметрами.
     * @return строка запроса.
     */
    private String createInsertQuery(InsertData insertData) {
        return new QueryInsert(insertData.getArguments()).setInsertData(insertData).getQuery();
    }

    /**
     * Преобразует массив данных insert в массив (одна строка)
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
     * Список полей через запятую с кавычками из массива полей.
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
