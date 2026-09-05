package org.agty.sql.data;

import org.agty.sql.support.SqlIdentifierValidator;
import org.agty.sql.support.LegacySqlFormatter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Arguments for the query.
 *
 * <p>This mutable builder is not thread-safe. Do not modify or execute the same
 * instance concurrently.</p>
 */
public class Arguments {

    /**
     * @deprecated Use {@link #setTable(String)} and {@link #getTable()}.
     * Direct assignments are validated when the value is read.
     */
    @Deprecated
    public String table;

    /** Fields for query (SELECT fields FROM)*/
    private String fields;

    /** Action field */
    private String actionField;

    /** Primary key*/
    private String primaryKey;

    /** A WHERE clause*/
    private String where = "";

    /** A WHERE clause with JDBC placeholders*/
    private String preparedWhere = "";

    /** Values bound to prepared WHERE placeholders*/
    private final List<Object> whereParameters = new LinkedList<>();

    /** A HAVING clause*/
    private String having = "";

    /** A GROUP BY clause*/
    private String groupBy = "";

    /** A GROUP BY clause*/
    private String orderBy = "";

    /** Legacy-rendered and prepared values for write queries. */
    private final ArgumentDataStore data = new ArgumentDataStore();

    /** Columns set*/
    private final List<String> columns = new LinkedList<>();

    /** RAW query*/
    private String query;

    /** Values bound to placeholders in a raw query*/
    private final List<Object> queryParameters = new LinkedList<>();

    /** All values converted to string*/
    private boolean convertValueToString = false;

    /** Emulate mode*/
    private boolean emulateMode = false;

    /** Don't encode data in values*/
    private boolean noStringEncode = false;

    /** Don't rebuild query*/
    private boolean noRebuildQuery = false;

    /** Force rebuild query*/
    private boolean forceRebuildQuery = false;

    /** Returns last insert ID*/
    private boolean returnLastInsertId = false;

    /** Execute high-level queries through PreparedStatement*/
    private boolean statementPrepare = false;

    /** Limit object*/
    private final Limit limit = new Limit();

    /**
     * Constructor
     */
    public Arguments() {}

    /**
     * The builder aka a new Arguments object
     * @return Arguments
     */
    public static Arguments builder() {
        return new Arguments();
    }

    /**
     * Whether high-level operations should use JDBC prepared statements.
     * The default is {@code false}, which preserves the legacy query rendering.
     *
     * @return {@code true} when prepared execution is enabled
     */
    public boolean useStatementPrepare() {
        return statementPrepare;
    }

    /**
     * Enables or disables prepared execution for high-level operations.
     *
     * @param statementPrepare {@code true} to render values as {@code ?} placeholders
     * @return current arguments
     */
    public Arguments useStatementPrepare(boolean statementPrepare) {
        this.statementPrepare = statementPrepare;
        return this;
    }

    /**
     * Don't encode data in values
     * @return bool
     */
    public boolean noStringEncode() {
        return noStringEncode;
    }

    /**
     * Don't encode data in values
     * @param noStringEncode bool
     * @return QueryArguments
     */
    public Arguments setNoStringEncode(boolean noStringEncode) {
        this.noStringEncode = noStringEncode;
        return this;
    }

    /**
     * Returns last insert ID
     * @return bool
     */
    public boolean returnLastInsertId() {
        return returnLastInsertId;
    }

    /**
     * Returns last insert ID
     *
     * @param returnLastInsertId bool
     * @return QueryArguments
     */
    public Arguments setReturnLastInsertId(boolean returnLastInsertId) {
        this.returnLastInsertId = returnLastInsertId;
        return this;
    }

    /**
     * Don't rebuild query
     * @return bool
     */
    public boolean noRebuildQuery() {
        return noRebuildQuery;
    }

    /**
     * Don't rebuild query
     * @param noRebuildQuery bool
     * @return QueryArguments
     */
    public Arguments setNoRebuildQuery(boolean noRebuildQuery) {
        this.noRebuildQuery = noRebuildQuery;
        return this;
    }

    /**
     * Force rebuild query
     * @return bool
     */
    public boolean forceRebuildQuery() {
        return forceRebuildQuery;
    }

    /**
     * @deprecated use {@link #forceRebuildQuery()}
     * @return whether forced query rebuilding is enabled
     */
    @Deprecated
    public boolean forceRequery() {
        return forceRebuildQuery();
    }

    /**
     * Force rebuild query
     * @param forceRebuildQuery bool
     * @return QueryArguments
     */
    public Arguments setForceRebuildQuery(boolean forceRebuildQuery) {
        this.forceRebuildQuery = forceRebuildQuery;
        return this;
    }

    /**
     * Таблица.
     *
     * @param table имя таблицы.
     * @return current arguments
     */
    public Arguments setTable(String table) {
        this.table = SqlIdentifierValidator.requireTable(table);
        return this;
    }

    /**
     * Текущая таблица.
     * Пример: my_table, {my_table}.
     *
     * @return имя таблицы.
     */
    public String getTable() {
       return table == null ? null : SqlIdentifierValidator.requireTable(table);
    }

    /**
     * Активное поле.
     *
     * @param actionField имя поля.
     * @return current arguments
     */
    public Arguments setActionField(String actionField) {
        this.actionField = SqlIdentifierValidator.requireColumn(actionField, "action field");
        return this;
    }

    /**
     * Текуще активное поле.
     *
     * @return имя активного поля.
     */
    public String getActionField() {
       return actionField;
    }

    /**
     * Добавить колонку.
     *
     * @param column имя колонки.
     * @return current arguments
     */
    public Arguments addColumn(String column) {
        columns.add(SqlIdentifierValidator.requireColumn(column, "column"));
        return this;
    }

    /**
     * Текущие колонки.
     *
     * @return копия массива колонок.
     */
    public List<String> getColumns() {
        return new LinkedList<String>(columns);
    }

    /**
     * Проверка наличия полей
     *
     * @return true если активное поле присутствует.
     */
    public boolean hasColumns() {
        return !columns.isEmpty();
    }

    /**
     * Проверка наличия активного поля.
     *
     * @return true если активное поле присутствует.
     */
    public boolean hasActionField() {
        return getActionField() != null && !getActionField().isEmpty();
    }

    /**
     * Текущая таблица + префикс + кавычки.
     * Пример: "pfx_my_table", `pfx_my_table`.
     *
     * @return имя таблицы.
     * @param sqlQueryRebuild query rebuilder
     */
    public String getTablePrefix(SqlQueryRebuild sqlQueryRebuild) {
        return sqlQueryRebuild.tablePrefix( getTable() );
    }

    /**
     * Текущая таблица + префикс, без кавычек.
     * Пример: pfx_my_table.
     *
     * @return имя таблицы.
     * @param sqlQueryRebuild query rebuilder
     */
    public String getTablePrefixNoQuote(SqlQueryRebuild sqlQueryRebuild) {
       return sqlQueryRebuild.tablePrefixNoQuote( getTable() );
    }

    /**
     * Проверка наличия таблицы. Обязательный параметр во всех классах.
     *
     * @return true если таблица создана.
     */
    public boolean hasTable() {
       return getTable() != null && !getTable().isEmpty();
    }

    /**
     * Основной ключ.
     *
     * @return основной ключ.
     */
    public String getPrimaryKey() {
        return primaryKey;
    }

    /**
     * Назначить основной ключ.
     *
     * @param primaryKey основной ключ.
     * @return current arguments
     */
    public Arguments setPrimaryKey(String primaryKey) {
        this.primaryKey = SqlIdentifierValidator.requireColumn(primaryKey, "primary key");
        return this;
    }

    /**
     * Поля таблицы.
     *
     * @param fields поля.
     * @return current arguments
     */
    public Arguments setFields(String fields) {
        this.fields = fields == null || fields.isBlank()
                ? fields
                : SqlIdentifierValidator.requireFieldList(fields);
        return this;
    }

    /**
     * Sets an explicitly trusted fields expression.
     *
     * @param fields trusted fields fragment
     * @return current arguments
     */
    public Arguments setFields(SqlExpression fields) {
        this.fields = requireExpression(fields, "fields");
        return this;
    }

    /**
     * Текущие поля таблицы.
     * По умолчанию: *.
     *
     * @return поля.
     */
    public String getFields() {
        return fields != null && !fields.isEmpty() ? fields : "*";
    }

    /**
     * Условие выборки.
     *
     * @param where условие выборки.
     * @return current arguments
     * @deprecated This overload accepts trusted raw SQL. Use parameterized
     * {@link #setWhere(String, Object...)} or explicitly mark a static fragment
     * with {@link #setWhere(SqlExpression)}.
     */
    @Deprecated
    public Arguments setWhere(String where) {
        if (where != null && !where.isBlank()) {
            throw new IllegalArgumentException(
                    "WHERE is a SQL expression; use parameters or setWhere(SqlExpression.trusted(...))"
            );
        }
        this.where = where;
        this.preparedWhere = where;
        this.whereParameters.clear();
        return this;
    }

    /**
     * Sets an explicitly trusted WHERE expression.
     *
     * @param where trusted WHERE expression
     * @return current arguments
     */
    public Arguments setWhere(SqlExpression where) {
        return setTrustedWhere(requireExpression(where, "WHERE"));
    }
    /**
     * Условие выборки.
     *
     * @param query условие выборки.
     * @param args массив параметров.
     * @return current arguments
     */
    public Arguments setWhere(String query, Object ...args) {
        this.where = renderWhere(query, args);
        this.preparedWhere = query;
        this.whereParameters.clear();
        Collections.addAll(this.whereParameters, args);
        return this;
    }

    /**
     * Условие выборки с последовательным добавлением.
     *
     * @param where условие выборки.
     * @return current arguments
     * @deprecated This overload appends trusted raw SQL. Use the parameterized
     * overload or {@link #appendWhere(SqlExpression)}.
     */
    @Deprecated
    public Arguments appendWhere(String where) {
        if (where != null && !where.isBlank()) {
            throw new IllegalArgumentException(
                    "WHERE is a SQL expression; use parameters or appendWhere(SqlExpression.trusted(...))"
            );
        }
        this.where += where;
        this.preparedWhere += where;
        return this;
    }

    /**
     * Appends an explicitly trusted WHERE expression.
     *
     * @param where trusted WHERE expression
     * @return current arguments
     */
    public Arguments appendWhere(SqlExpression where) {
        String expression = requireExpression(where, "WHERE");
        this.where += expression;
        this.preparedWhere += expression;
        return this;
    }

    /**
     * Условие выборки с последовательным добавлением.
     *
     * @param where условие выборки.
     * @param args values for the appended placeholders
     * @return current arguments
     */
    public Arguments appendWhere(String where, Object ...args) {
        this.where += renderWhere(where, args);
        this.preparedWhere += where;
        Collections.addAll(this.whereParameters, args);
        return this;
    }

    /**
     * Текущее условие выборки.
     * По умолчанию: пустая строка.
     *
     * @return условие выборки.
     */
    public String getWhere() {
        return useStatementPrepare() ? preparedWhere : where;
    }

    /**
     * Values for {@code ?} placeholders in the prepared WHERE clause.
     *
     * @return parameter values in declaration order
     */
    public List<Object> getWhereParameters() {
        return new LinkedList<>(whereParameters);
    }

    /**
     * Проверка наличия условия выборки.
     *
     * @return true если условие выборки задано.
     */
    public boolean hasWhere() {
        return getWhere() != null && !getWhere().isEmpty();
    }

    /**
     * Постусловие HAVING.
     *
     * @param having постусловие.
     * @return current arguments
     * @deprecated Use {@link #setHaving(SqlExpression)} for an explicitly
     * trusted SQL expression.
     */
    @Deprecated
    public Arguments setHaving(String having) {
        if (having != null && !having.isBlank()) {
            throw new IllegalArgumentException(
                    "HAVING is a SQL expression; use setHaving(SqlExpression.trusted(...))"
            );
        }
        this.having = having;
        return this;
    }

    /**
     * Sets an explicitly trusted HAVING expression.
     *
     * @param having trusted HAVING fragment
     * @return current arguments
     */
    public Arguments setHaving(SqlExpression having) {
        this.having = requireExpression(having, "HAVING");
        return this;
    }

    /**
     * Текущее постусловие HAVING.
     *
     * @return условие сортировки.
     */
    public String getHaving() {
        return having;
    }

    /**
     * Группировка в запросе.
     *
     * @param groupBy условие группировки.
     * @return current arguments
     */
    public Arguments setGroupBy(String groupBy) {
        this.groupBy = groupBy == null || groupBy.isBlank()
                ? groupBy
                : SqlIdentifierValidator.requireGroupBy(groupBy);
        return this;
    }

    /**
     * Sets an explicitly trusted GROUP BY expression.
     *
     * @param groupBy trusted grouping fragment
     * @return current arguments
     */
    public Arguments setGroupBy(SqlExpression groupBy) {
        this.groupBy = requireExpression(groupBy, "GROUP BY");
        return this;
    }

    /**
     * Текущее условие группировки.
     *
     * @return условие сортировки.
     */
    public String getGroupBy() {
        return groupBy;
    }

    /**
     * Сортировка в запросе.
     *
     * @param orderBy условие сортировки.
     * @return current arguments
     */
    public Arguments setOrderBy(String orderBy) {
        this.orderBy = orderBy == null || orderBy.isBlank()
                ? orderBy
                : SqlIdentifierValidator.requireOrderBy(orderBy);
        return this;
    }

    /**
     * Sets an explicitly trusted ORDER BY expression.
     *
     * @param orderBy trusted sorting fragment
     * @return current arguments
     */
    public Arguments setOrderBy(SqlExpression orderBy) {
        this.orderBy = requireExpression(orderBy, "ORDER BY");
        return this;
    }

    /**
     * Текущее условие сортировки.
     *
     * @return условие сортировки.
     */
    public String getOrderBy() {
        return orderBy;
    }

    /**
     * Проверка наличия сортировки.
     *
     * @return true если сортировка задана.
     */
    public boolean hasOrderBy() {
        return getOrderBy() != null && !getOrderBy().isEmpty();
    }

    /**
     * Проверка наличия группировки.
     *
     * @return true если группировка задана.
     */
    public boolean hasGroupBy() {
        return getGroupBy() != null && !getGroupBy().isEmpty();
    }

    /**
     * Проверка наличия постусловия HAVING.
     *
     * @return true если постусловие задано.
     */
    public boolean hasHaving() {
        return getHaving() != null && !getHaving().isEmpty();
    }

    /**
     * Проверка наличия постусловия HAVING и условия GROUP BY.
     *
     * @return true если все параметры заданы.
     */
    public boolean hasHavingAndGroupBy() {
        return hasGroupBy() && hasHaving();
    }

    /**
     * Проверка наличия ORDER, GROUP и постусловия HAVING.
     *
     * @return true если все параметры заданы.
     */
    public boolean hasOrderGroupHaving() {
        return hasOrderBy() && hasGroupBy() && hasHaving();
    }

    /**
     * Проверка наличия хотя бы одного из ORDER, GROUP и постусловия HAVING.
     *
     * @return true если задано хотя бы один из параметров.
     */
    public boolean hasOneOfOrderGroupHaving() {
        return hasOrderBy() || hasGroupBy() || hasHaving();
    }

    /**
     * Ограничение выборки. Драйвер БД сам определяет, как использовать
     * синтаксис ограничения. Передается в виде:
     *      - 10 // выбрать 10 строк
     *      - 200,10 // выбрать 10 строк, пропустив 200
     *      - 0,10 // выбрать 10 строк
     *
     * @param limitString ограничение выборки.
     * @return current arguments
     */
    public Arguments setLimit(String limitString) {
        if (limitString.contains(",")) {
            String[] split = limitString.split(",");
            limit.setLimit( Integer.parseInt(split[1].trim()) );
            limit.setOffset( Integer.parseInt(split[0].trim()) );
        } else {
            limit.setLimit( Integer.parseInt(limitString.trim()) );
        }
        return this;
    }

    public Arguments setLimit(int limit) {
        this.limit.setLimit(limit);
        return this;
    }

    public Arguments setOffset(int offset) {
        this.limit.setOffset(offset);
        return this;
    }

    /**
     * Объект ограничения выборки.
     *
     * @return ограничение выборки.
     */
    public Limit getLimitObject() {
        return limit;
    }

    /**
     * Текущее ограничение выборки.
     *
     * @return кол-во строк.
     */
    public Integer getLimit() {
        return limit.getLimit();
    }

    /**
     * Кол-во пропущенных строк.
     *
     * @return кол-во строк.
     */
    public Integer getOffset() {
        return limit.getOffset();
    }

    /**
     * Текущее ограничение выборки в виде строкового значения.
     *
     * @return кол-во строк.
     */
    public String getLimitString() {
        return limit.getLimitString();
    }

    /**
     * Кол-во пропущенных строк в виде строкового значения.
     *
     * @return кол-во строк.
     */
    public String getOffsetString() {
        return limit.getOffsetString();
    }

    /**
     * Проверка наличия ограничения выборки.
     *
     * @return true если ограничение выборки задано.
     */
    public boolean hasLimit() {
        return limit.hasLimit();
    }

    /**
     * Проверка наличия пропуска строк.
     *
     * @return true если пропуск строк задано.
     */
    public boolean hasOffset() {
        return limit.hasOffset();
    }

    /**
     * Проверка наличия ограничений выборки и пропущенных строк.
     *
     * @return true если ограничения заданы.
     */
    public boolean hasLimitOffset() {
        return limit.hasLimitAndOffset();
    }

    /**
     * Наличие ограничения выборки ИЛИ пропущенных строк.
     *
     * @return  true если ограничения заданы.
     */
    public boolean hasLimitOrOffset() {
        return hasLimit() || hasOffset();
    }

    /**
     * Произвольный запрос. Например:
     *      SELECT user.name, user.id, pass.serial, pass.number
     *      FROM my_users as user
     *      LEFT JOIN my_passports as pass ON (pass.id_user = user.id)
     *      WHERE user.id_town = 123
     *      ORDER BY user.datecreate DESC
     *      LIMIT 100
     *
     * @param query произвольный запрос.
     * @return current arguments
     * @deprecated This overload accepts trusted raw SQL. Prefer prepared
     * {@link #setQuery(String, Object...)} or {@link #setQuery(SqlExpression)}.
     */
    @Deprecated
    public Arguments setQuery(String query) {
        if (query != null && !query.isBlank()) {
            throw new IllegalArgumentException(
                    "Raw query must be explicitly trusted with setQuery(SqlExpression.trusted(...))"
            );
        }
        this.query = query;
        this.queryParameters.clear();
        return this;
    }

    /**
     * Sets an explicitly trusted complete SQL query.
     *
     * @param query trusted query
     * @return current arguments
     */
    public Arguments setQuery(SqlExpression query) {
        this.query = requireExpression(query, "query");
        this.queryParameters.clear();
        return this;
    }

    /**
     * Sets a raw query and values for its JDBC placeholders.
     * Parameters are used only when {@link #useStatementPrepare(boolean)} is enabled.
     *
     * @param query raw query containing {@code ?} placeholders
     * @param parameters placeholder values in declaration order
     * @return current arguments
     */
    public Arguments setQuery(String query, Object ...parameters) {
        this.query = query;
        this.queryParameters.clear();
        Collections.addAll(this.queryParameters, parameters);
        return this;
    }

    /**
     * Текущий произвольный запрос.
     *
     * @return произвольный запрос.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Values for {@code ?} placeholders in the raw query.
     *
     * @return parameter values in declaration order
     */
    public List<Object> getQueryParameters() {
        return new LinkedList<>(queryParameters);
    }

    /**
     * Проверка наличия запроса query. Во всех классах query имеет приоритет.
     *
     * @return true если запрос создан и не пустой.
     */
    public boolean hasQuery() {
        return query != null && !query.isEmpty();
    }

    /**
     * Adds a string value.
     * @param field column name
     * @param value value
     * @return current arguments
     */
    public Arguments addData(String field, String value) {
        return addDataString(field, value);
    }

    /**
     * Adds an integer value.
     * @param field column name
     * @param value value
     * @return current arguments
     */
    public Arguments addData(String field, Integer value) {
        return addDataInt(field, value);
    }

    /**
     * Adds a long value.
     * @param field column name
     * @param value value
     * @return current arguments
     */
    public Arguments addData(String field, Long value) {
        return addDataLong(field, value);
    }

    /**
     * Adds a short value.
     * @param field column name
     * @param value value
     * @return current arguments
     */
    public Arguments addData(String field, Short value) {
        return addDataShort(field, value);
    }

    /**
     * Adds a boolean value.
     * @param field column name
     * @param value value
     * @return current arguments
     */
    public Arguments addData(String field, Boolean value) {
        return addDataBoolean(field, value);
    }

    /**
     * Adds a float value.
     * @param field column name
     * @param value value
     * @return current arguments
     */
    public Arguments addData(String field, Float value) {
        return addDataFloat(field, value);
    }

    /**
     * Adds a double value.
     * @param field column name
     * @param value value
     * @return current arguments
     */
    public Arguments addData(String field, Double value) {
        return addDataDouble(field, value);
    }

    /**
     * Adds a character value.
     * @param field column name
     * @param value value
     * @return current arguments
     */
    public Arguments addData(String field, Character value) {
        return addDataChar(field, value);
    }

    /**
     * Adds a dynamically typed value after validating its runtime type.
     *
     * @param field column name
     * @param value supported scalar value
     * @return current arguments
     * @throws IllegalArgumentException when the value is not a supported scalar
     */
    public Arguments addData(String field, Object value) {
        if (value == null) {
            dataPut(field, null);
            return this;
        }
        if (value instanceof String) {
            return addDataString(field, value);
        }
        if (value instanceof Number) {
            return addDataDecimal(field, value);
        }
        if (value instanceof Boolean) {
            return addDataBoolean(field, value);
        }
        if (value instanceof Character) {
            return addDataChar(field, value);
        }

        throw ArgumentValueNormalizer.unsupportedDataType(field, value);
    }

    /**
     * Adds a value only when its runtime type is {@link String}.
     * @param field column name
     * @param value value to validate
     * @return current arguments
     */
    public Arguments addDataString(String field, Object value) {
        String stringValue = ArgumentValueNormalizer.requireType(field, value, String.class);
        dataPut(field, stringValue, stringValue);
        return this;
    }

    /**
     * Adds a value only when its runtime type is {@link Integer}.
     * @param field column name
     * @param value value to validate
     * @return current arguments
     */
    public Arguments addDataInt(String field, Object value) {
        dataPut(field, ArgumentValueNormalizer.requireType(field, value, Integer.class));
        return this;
    }

    /**
     * Alias for {@link #addDataInt(String, Object)}.
     * @param field column name
     * @param value value to validate
     * @return current arguments
     */
    public Arguments addDataInteger(String field, Object value) {
        return addDataInt(field, value);
    }

    /**
     * Adds a value only when its runtime type is {@link Long}.
     * @param field column name
     * @param value value to validate
     * @return current arguments
     */
    public Arguments addDataLong(String field, Object value) {
        dataPut(field, ArgumentValueNormalizer.requireType(field, value, Long.class));
        return this;
    }

    /**
     * Adds a value only when its runtime type is {@link Short}.
     * @param field column name
     * @param value value to validate
     * @return current arguments
     */
    public Arguments addDataShort(String field, Object value) {
        dataPut(field, ArgumentValueNormalizer.requireType(field, value, Short.class));
        return this;
    }

    /**
     * Adds a value only when its runtime type is {@link Byte}.
     * @param field column name
     * @param value value to validate
     * @return current arguments
     */
    public Arguments addDataByte(String field, Object value) {
        dataPut(field, ArgumentValueNormalizer.requireType(field, value, Byte.class));
        return this;
    }

    /**
     * Adds a value only when its runtime type is {@link Boolean}.
     * @param field column name
     * @param value value to validate
     * @return current arguments
     */
    public Arguments addDataBoolean(String field, Object value) {
        dataPut(field, ArgumentValueNormalizer.requireType(field, value, Boolean.class));
        return this;
    }

    /**
     * Alias for {@link #addDataBoolean(String, Object)}.
     * @param field column name
     * @param value value to validate
     * @return current arguments
     */
    public Arguments addDataBool(String field, Object value) {
        return addDataBoolean(field, value);
    }

    /**
     * Adds a value only when its runtime type is {@link Float}.
     * @param field column name
     * @param value value to validate
     * @return current arguments
     */
    public Arguments addDataFloat(String field, Object value) {
        Float number = ArgumentValueNormalizer.requireType(field, value, Float.class);
        ArgumentValueNormalizer.validateFiniteNumber(field, number);
        dataPut(field, number);
        return this;
    }

    /**
     * Adds a value only when its runtime type is {@link Double}.
     * @param field column name
     * @param value value to validate
     * @return current arguments
     */
    public Arguments addDataDouble(String field, Object value) {
        Double number = ArgumentValueNormalizer.requireType(field, value, Double.class);
        ArgumentValueNormalizer.validateFiniteNumber(field, number);
        dataPut(field, number);
        return this;
    }

    /**
     * Adds any numeric value. Standard {@link Number} implementations retain
     * their type; other implementations are normalized to {@link BigDecimal}.
     *
     * @param field column name
     * @param value numeric value to validate
     * @return current arguments
     */
    public Arguments addDataDecimal(String field, Object value) {
        Number number = ArgumentValueNormalizer.requireType(field, value, Number.class);
        dataPut(field, ArgumentValueNormalizer.normalizeNumber(field, number));
        return this;
    }

    /**
     * Alias for {@link #addDataDecimal(String, Object)}.
     * @param field column name
     * @param value numeric value to validate
     * @return current arguments
     */
    public Arguments addDataNumber(String field, Object value) {
        return addDataDecimal(field, value);
    }

    /**
     * Adds a value only when its runtime type is {@link BigDecimal}.
     * @param field column name
     * @param value value to validate
     * @return current arguments
     */
    public Arguments addDataBigDecimal(String field, Object value) {
        dataPut(field, ArgumentValueNormalizer.requireType(field, value, BigDecimal.class));
        return this;
    }

    /**
     * Adds a value only when its runtime type is {@link BigInteger}.
     * @param field column name
     * @param value value to validate
     * @return current arguments
     */
    public Arguments addDataBigInteger(String field, Object value) {
        dataPut(field, ArgumentValueNormalizer.requireType(field, value, BigInteger.class));
        return this;
    }

    /**
     * Adds a value only when its runtime type is {@link Character}.
     * @param field column name
     * @param value value to validate
     * @return current arguments
     */
    public Arguments addDataChar(String field, Object value) {
        dataPut(field, ArgumentValueNormalizer.requireType(field, value, Character.class));
        return this;
    }

    /**
     * Alias for {@link #addDataChar(String, Object)}.
     * @param field column name
     * @param value value to validate
     * @return current arguments
     */
    public Arguments addDataCharacter(String field, Object value) {
        return addDataChar(field, value);
    }

    /**
     * Adds an explicit SQL NULL value without overload ambiguity.
     * @param field column name
     * @return current arguments
     */
    public Arguments addDataNull(String field) {
        dataPut(field, null);
        return this;
    }

    /**
     * Добавить boolean-значение в формате, подходящем для выбранного драйвера.
     *
     * @param field имя поля.
     * @param value boolean-значение.
     * @param driver имя драйвера.
     * @return текущий объект Arguments.
     */
    public Arguments addData(String field, boolean value, String driver) {
        return addData(field, Arguments.getBooleanValueForDriver(value, driver));
    }

    /**
     * @deprecated use {@link #addData(String, String)}
     * @param field column name
     * @param value value
     * @return current arguments
     */
    @Deprecated
    public Arguments setData(String field, String value) {
        return addData(field, value);
    }

    /**
     * @deprecated use {@link #addData(String, Integer)}
     * @param field column name
     * @param value value
     * @return current arguments
     */
    @Deprecated
    public Arguments setData(String field, Integer value) {
        return addData(field, value);
    }

    /**
     * @deprecated use {@link #addData(String, Long)}
     * @param field column name
     * @param value value
     * @return current arguments
     */
    @Deprecated
    public Arguments setData(String field, Long value) {
        return addData(field, value);
    }

    /**
     * @deprecated use {@link #addData(String, Short)}
     * @param field column name
     * @param value value
     * @return current arguments
     */
    @Deprecated
    public Arguments setData(String field, Short value) {
        return addData(field, value);
    }

    /**
     * @deprecated use {@link #addData(String, Boolean)}
     * @param field column name
     * @param value value
     * @return current arguments
     */
    @Deprecated
    public Arguments setData(String field, Boolean value) {
        return addData(field, value);
    }

    /**
     * @deprecated use {@link #addData(String, Float)}
     * @param field column name
     * @param value value
     * @return current arguments
     */
    @Deprecated
    public Arguments setData(String field, Float value) {
        return addData(field, value);
    }

    /**
     * @deprecated use {@link #addData(String, Double)}
     * @param field column name
     * @param value value
     * @return current arguments
     */
    @Deprecated
    public Arguments setData(String field, Double value) {
        return addData(field, value);
    }

    /**
     * @deprecated use {@link #addData(String, Character)}
     * @param field column name
     * @param value value
     * @return current arguments
     */
    @Deprecated
    public Arguments setData(String field, Character value) {
        return addData(field, value);
    }

    private void dataPut(String field, Object value) {
        dataPut(field, value, value);
    }

    private void dataPut(String field, Object legacyValue, Object preparedValue) {
        data.put(field, legacyValue, preparedValue);
    }

    private String requireExpression(SqlExpression expression, String role) {
        if (expression == null) {
            throw new IllegalArgumentException(role + " expression must not be null");
        }
        return expression.sql();
    }

    private Arguments setTrustedWhere(String expression) {
        this.where = expression;
        this.preparedWhere = expression;
        this.whereParameters.clear();
        return this;
    }

    private String renderWhere(String template, Object... values) {
        if (useStatementPrepare() || template.indexOf('?') >= 0) {
            return template;
        }
        return LegacySqlFormatter.format(template, values);
    }

    /**
     * Возвращает boolean-значение в формате записи, ожидаемом драйвером БД.
     *
     * <p>Для `mysql`, `mariadb`, `mssql`, `sqlite` и `h2` возвращаются `1`/`0`.
     * Для `pgsql` и `postgresql` возвращаются `true`/`false`.</p>
     *
     * @param value boolean-значение.
     * @param driver имя драйвера.
     * @return объект для записи в boolean-поле.
     */
    public static Object getBooleanValueForDriver(boolean value, String driver) {
        if (driver == null || driver.isBlank()) {
            throw new IllegalArgumentException("Driver name is required for boolean value conversion.");
        }

        return switch (driver.trim().toLowerCase(Locale.ROOT)) {
            case "mysql", "mariadb", "mssql", "sqlserver", "sqlite", "h2" -> value ? 1 : 0;
            case "pgsql", "postgres", "postgresql" -> value;
            default -> throw new IllegalArgumentException(
                    "Unsupported driver for boolean value conversion: " + driver
            );
        };
    }

    /**
     * Удаление поля из данных.
     *
     * @param field имя поля.
     */
    public void removeData(String field) {
        data.remove(field);
    }

    /**
     * @deprecated use {@link #removeData(String)}
     * @param field column name
     */
    @Deprecated
    public void removeFromData(String field) {
        removeData(field);
    }

    /**
     * Удаление всех данных.
     */
    public void clearData() {
        data.clear();
    }

    /**
     * Проверка наличия данных.
     *
     * @return true если данные заданы.
     */
    public boolean hasData() {
        return !data.isEmpty();
    }

    /**
     * Возвращает копию текущего состояния массива с данными.
     *
     * @return копия коллекции массива с данными.
     */
    public LinkedHashMap<String, Object> getDataMap() {
        return data.copy(useStatementPrepare());
    }

    /**
     * @deprecated use {@link #getDataMap()}
     * @return copied data map
     */
    @Deprecated
    public LinkedHashMap<String, Object> getDataArray() {
        return getDataMap();
    }

    /**
     * Получить содержимое данных по ключу.
     *
     * @param key ключ данных.
     * @return объект содержимого.
     */
    public Object getData(String key) {
        return data.get(key, useStatementPrepare());
    }

    /**
     * @deprecated use {@link #getData(String)}
     * @param key data key
     * @return stored value
     */
    @Deprecated
    public Object getFromData(String key) {
        return getData(key);
    }

    /**
     * Все ключи в данных.
     *
     * @return массив ключей.
     */
    public LinkedList<String> getDataKeys() {
        return data.keys();
    }

    /**
     * Проверка наличие ключей.
     * Аналогично проверке наличия данных.
     *
     * @return true если данные заданы.
     */
    public boolean hasDataKeys() {
        return hasData();
    }

    /**
     * Все значения в данных.
     *
     * @return массив значений.
     */
    public LinkedList<Object> getDataValues() {
        return data.values(useStatementPrepare());
    }

    /**
     * Проверка наличие значений.
     * Аналогично проверке наличия данных.
     *
     * @return true если данные заданы.
     */
    public boolean hasDataValues() {
        return hasData();
    }

    /**
     * Размер данных по количеству элементов.
     *
     * @return размер данных.
     */
    public int dataSize() {
        return data.size();
    }

    /**
     * Если задан параметр, тогда все возвращаемые объекты методов fetch, listObject, ..., будут
     * возвращаться в виде строки.
     *
     * @param valueToString true если необходимо возвращать данные в виде строки.
     * @return current arguments
     */
    public Arguments convertValueToString(boolean valueToString) {
        this.convertValueToString = valueToString;
        return this;
    }

    /**
     * Reports whether fetched values should be converted to strings.
     *
     * @return whether string conversion is enabled
     */
    public boolean convertValueToString() {
        return convertValueToString;
    }

    /**
     * Режим эмуляции. Запросы не отправляются в базу.
     *
     * @param emulateMode true если режим эмуляции включен.
     * @return текущий объект Arguments.
     */
    public Arguments setEmulateMode(boolean emulateMode) {
        this.emulateMode = emulateMode;
        return this;
    }

    /**
     * Режим эмуляции. Запросы не отправляются в базу.
     * @return bool
     */
    public boolean isEmulateMode() {
        return emulateMode;
    }

    @Override
    public String toString() {

        StringBuilder toString = new StringBuilder("Arguments {\n");

        //Основные параметры (только те, что заполнены)
        toString.append("\tParams: {\n");

        toString.append("\t\ttable = ");
        toString.append(table);
        toString.append("\n");

        toString.append("\t\tfields = ");
        toString.append(fields);
        toString.append("\n");

        toString.append("\t\tactionField = ");
        toString.append(actionField);
        toString.append("\n");

        toString.append("\t\tprimaryKey = ");
        toString.append(primaryKey);
        toString.append("\n");

        toString.append("\t\twhere = ");
        toString.append(getWhere());
        toString.append("\n");

        toString.append("\t\thaving = ");
        toString.append(having);
        toString.append("\n");

        toString.append("\t\tgroupBy = ");
        toString.append(groupBy);
        toString.append("\n");

        toString.append("\t\t");
        toString.append("orderBy");
        toString.append(" = ");
        toString.append(orderBy);
        toString.append("\n");

        toString.append("\t\t");
        toString.append("limit");
        toString.append(" = ");
        toString.append(limit.getLimit());
        toString.append("\n");

        toString.append("\t\t");
        toString.append("offset");
        toString.append(" = ");
        toString.append(limit.getOffset());
        toString.append("\n");

        toString.append("\t}\n");

        //Данные
        toString.append("\tData: {\n");

        for (Map.Entry<String, Object> entry: data.legacyEntries()) {
            toString.append("\t\t");
            toString.append(entry.getKey());
            toString.append(" = ");
            toString.append(entry.getValue());
            toString.append("\n");
        }

        toString.append("\t}\n");
        toString.append("}\n");

        //Поля
        toString.append("\tColumns: {\n\t");
        toString.append(columns);
        toString.append("\t}\n");
        toString.append("}\n");

        return toString.toString();
    }
}
