package org.agty.sql.data;

import java.util.*;

/**
 * Arguments for the query
 */
public class Arguments {

    /** Table */
    public String table;

    /** Fields for query (SELECT fields FROM)*/
    private String fields;

    /** Action field */
    private String actionField;

    /** Primary key*/
    private String primaryKey;

    /** A WHERE clause*/
    private String where = "";

    /** A HAVING clause*/
    private String having = "";

    /** A GROUP BY clause*/
    private String groupBy = "";

    /** A GROUP BY clause*/
    private String orderBy = "";

    /** Data for query*/
    private final Map<String, Object> data = new LinkedHashMap<>();

    /** Columns set*/
    private final List<String> columns = new LinkedList<>();

    /** RAW query*/
    private String query;

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
    public boolean forceRequery() {
        return forceRebuildQuery;
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
     */
    public Arguments setTable(String table) {
        this.table = table;
        return this;
    }

    /**
     * Текущая таблица.
     * Пример: my_table, {my_table}.
     *
     * @return имя таблицы.
     */
    public String getTable() {
       return table;
    }

    /**
     * Активное поле.
     *
     * @param actionField имя поля.
     */
    public Arguments setActionField(String actionField) {
        this.actionField = actionField;
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
     */
    public Arguments addColumn(String column) {
        columns.add(column);
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
     */
    public String getTablePrefix(SqlQueryRebuild sqlQueryRebuild) {
        return sqlQueryRebuild.tablePrefix( getTable() );
    }

    /**
     * Текущая таблица + префикс, без кавычек.
     * Пример: pfx_my_table.
     *
     * @return имя таблицы.
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
     */
    public Arguments setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    /**
     * Поля таблицы.
     *
     * @param fields поля.
     */
    public Arguments setFields(String fields) {
        this.fields = fields;
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
     */
    public Arguments setWhere(String where) {
        this.where = where;
        return this;
    }
    /**
     * Условие выборки.
     *
     * @param query условие выборки.
     * @param args массив параметров.
     */
    public Arguments setWhere(String query, Object ...args) {
        this.where = query.formatted(args);
        return this;
    }

    /**
     * Условие выборки с последовательным добавлением.
     *
     * @param where условие выборки.
     */
    public Arguments appendWhere(String where) {
        this.where += where;
        return this;
    }

    /**
     * Условие выборки с последовательным добавлением.
     *
     * @param where условие выборки.
     */
    public Arguments appendWhere(String where, Object ...args) {
        this.where += query.formatted(args);
        return this;
    }

    /**
     * Текущее условие выборки.
     * По умолчанию: пустая строка.
     *
     * @return условие выборки.
     */
    public String getWhere() {
        return where;
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
     */
    public Arguments setHaving(String having) {
        this.having = having;
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
     */
    public Arguments setGroupBy(String groupBy) {
        this.groupBy = groupBy;
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
     */
    public Arguments setOrderBy(String orderBy) {
        this.orderBy = orderBy;
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
     */
    public Arguments setQuery(String query) {
        this.query = query;
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
     * Проверка наличия запроса query. Во всех классах query имеет приоритет.
     *
     * @return true если запрос создан и не пустой.
     */
    public boolean hasQuery() {
        return query != null && !query.isEmpty();
    }

    /**
     * Данные запроса. Где field имя поля в базе, value его содержание.
     *
     * @param field имя поля.
     * @param value содержимое поля.
     * @return текущий объект Arguments
     */
    public Arguments setData(String field, String value) {
        if (value != null && value.endsWith("\\")) value = value.replaceAll("\\\\+$", "");
        dataPut(field, value);
        return this;
    }

    public Arguments setData(String field, Integer value) {
        dataPut(field, value);
        return this;
    }

    public Arguments setData(String field, Long value) {
        dataPut(field, value);
        return this;
    }

    public Arguments setData(String field, Short value) {
        dataPut(field, value);
        return this;
    }

    public Arguments setData(String field, Boolean value) {
        dataPut(field, value);
        return this;
    }

    public Arguments setData(String field, Float value) {
        dataPut(field, value);
        return this;
    }

    public Arguments setData(String field, Double value) {
        dataPut(field, value);
        return this;
    }

    public Arguments setData(String field, Character value) {
        dataPut(field, value);
        return this;
    }

    private void dataPut(String field, Object value) {
        data.put(field, value);
    }

    /**
     * Удаление поля из данных.
     *
     * @param field имя поля.
     */
    public void removeFromData(String field) {
        data.remove(field);
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
    public LinkedHashMap<String, Object> getDataArray() {
        return new LinkedHashMap<String, Object>(data);
    }

    /**
     * Получить содержимое данных по ключу.
     *
     * @param key ключ данных.
     * @return объект содержимого.
     */
    public Object getFromData(String key) {
        return data.get(key);
    }

    /**
     * Все ключи в данных.
     *
     * @return массив ключей.
     */
    public LinkedList<String> getDataKeys() {
        return new LinkedList<String>(data.keySet());
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
        return new LinkedList<Object>(data.values());
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
     */
    public Arguments convertValueToString(boolean valueToString) {
        this.convertValueToString = valueToString;
        return this;
    }

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
        toString.append(where);
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

        for (Map.Entry<String, Object> entry: data.entrySet()) {
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
