package org.agty.sql.data;

import org.agty.sql.support.DebugMessages;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rebuild a query
 */
public class SqlQueryRebuild {
    /** A content into quotes counter*/
    private int contentQuoteCounter;

    /** Q query changed*/
    private boolean queryIsChanged = false;

    /** Original query*/
    private String query;

    /** Table prefix*/
    private String prefix = "";

    /** Quotes for a table*/
    private String quoteTable = "";

    /** Quotes for a column*/
    private String quoteColumn = "";

    /** Quotes for a value*/
    private String quoteValue = "";

    /** Debug mode*/
    private boolean debug = false;

    /**
     * Constructor.
     */
    public SqlQueryRebuild() {}

    /**
     * Constructor with a query string
     * @param query A query string
     */
    public SqlQueryRebuild(String query) {
        setQuery(query);
    }

    /**
     * If a query is changed return true
     * @return bool
     */
    public boolean isQueryIsChanged() {
        return queryIsChanged;
    }

    /**
     * If a query is changed set true
     * @param queryIsChanged bool
     */
    public void setQueryIsChanged(boolean queryIsChanged) {
        this.queryIsChanged = queryIsChanged;
    }

    /**
     * The query getter
     * @return String query
     */
    public String getQuery() {
        return query;
    }

    /**
     * The query setter
     * @param query A query string
     * @return SqlQueryRebuild
     */
    public SqlQueryRebuild setQuery(String query) {
        this.query = query;
        return this;
    }

    /**
     * Set debug mode
     * @param debug Debug mode
     * @return SqlQueryRebuild
     */
    public SqlQueryRebuild setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    /**
     * Check debug mode
     * @return boolean
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * A prefix getter
     * @return The prefix table
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * A prefix setter
     * @param prefix A prefix table
     * @return SqlQueryRebuild
     */
    public SqlQueryRebuild setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Table quotes getter
     * @return Quotes for a table
     */
    public String getQuoteTable() {
        return quoteTable;
    }

    /**
     * Table quotes setter
     * @param quoteTable Quotes for a table
     * @return SqlQueryRebuild
     */
    public SqlQueryRebuild setQuoteTable(String quoteTable) {
        this.quoteTable = quoteTable;
        return this;
    }

    /**
     * Column quotes getter
     * @return Quotes for a column
     */
    public String getQuoteColumn() {
        return quoteColumn;
    }

    /**
     * Column quotes setter
     * @param quoteColumn Quotes for a column
     * @return SqlQueryRebuild
     */
    public SqlQueryRebuild setQuoteColumn(String quoteColumn) {
        this.quoteColumn = quoteColumn;
        return this;
    }

    /**
     * Value quotes getter
     * @return Quotes for a value
     */
    public String getQuoteValue() {
        return quoteValue;
    }

    /**
     * Value quotes setter
     * @param quoteValue Quotes for a value
     * @return SqlQueryRebuild
     */
    public SqlQueryRebuild setQuoteValue(String quoteValue) {
        this.quoteValue = quoteValue;
        return this;
    }

    /**
     * Rebuild a query and return a result
     * @return A result
     */
    public String rebuildAndGet() {
        debugMessageQuery("Original query");

        HashMap<Integer, String> collect = new HashMap<>();

        //Замещаем все экранированные кавычки
        replaceQuoteEscape();

        //Находим все что между '
        replaceQuoteContent('\'', collect);

        //Находим все что между "
        replaceQuoteContent('"', collect);

        //Меняем все названия {таблиц} на названия таблиц с префиксом: `pfx_таблица`, "pfx_таблица"
        tablePrefix();

        //Меняем все названия [полей] на названия полей в кавычках: `поле`, "поле"
        columnsQuote();

        //Возвращаем строковые параметры в кавычках '/" на свои места, а так же возвращаем экранированные кавычки
        restoreValueContent(collect);

        return query;
    }

    /**
     * Debug messages
     * @param message Message
     */
    private void debugMessageQuery(String message) {
        if (isDebug()) DebugMessages.print("SqlQueryRebuild()", message + ": " + query);
    }

    /**
     * Replace quote escape
     */
    private void replaceQuoteEscape() {
        if (!query.contains("\\'") && !query.contains("\\\"")) return;
        query = query.replace("\\'", "$(singleQuote)").replace("\\\"", "$(doubleQuote)");
        setQueryIsChanged(true);
        debugMessageQuery("After replace quote escape");
    }

    /**
     * Replace content into values on content index
     * @param quote Quote
     * @param collect Values collection
     */
    private void replaceQuoteContent(char quote, HashMap<Integer, String> collect) {

        if (query.indexOf(quote) == -1) return;

        StringBuilder resultString = new StringBuilder();
        Matcher valueMatcher = Pattern.compile(quote + "([^" + quote + "]+)" + quote).matcher(query);

        while (valueMatcher.find()) {
            //Записываем в коллекцию найденное 'содержимое'
            collect.put(contentQuoteCounter, quote + valueMatcher.group(1) + quote);

            //Меняет 'содержимое'|"содержимое" на $(contentQuoteCounter): $(0), $(1), ...
            valueMatcher.appendReplacement(resultString, "\\$\\(" + contentQuoteCounter + "\\)");

            contentQuoteCounter++;
        }

        //Если были найдены строки с содержимым
        if (!resultString.isEmpty()) {
            valueMatcher.appendTail(resultString);
        }

        query = resultString.toString();

        setQueryIsChanged(true);

        debugMessageQuery("After " + quote);
    }

    /**
     * Restore content into values
     * @param collect Values collection
     */
    private void restoreValueContent(HashMap<Integer, String> collect) {
        if (!isQueryIsChanged()) return;

        for (Integer key : collect.keySet()) {
            query = query.replace("$(" + key + ")", collect.get(key));
        }

        query = query.replace("$(singleQuote)", "\\'").replace("$(doubleQuote)", "\\\"");

        debugMessageQuery("Restore value content");
    }

    /**
     * Ищет в строке названия таблиц в {} и меняет их на имя таблицы с префиксом в кавычках
     *  //SELECT * FROM `pfx_table_name`
     *  $AgtySQL.tablePrefix("SELECT * FROM {table_name}");
     *
     * @param query String query
     * @return String
     */
    final public String tablePrefix(String query) {
        return query.replaceAll(
                "\\{([a-zA-Z0-9_]+)}",
                getQuoteTable()
                        + getPrefix()
                        + "$1"
                        + getQuoteTable()
        );
    }

    /**
     * This query table prefix
     */
    private void tablePrefix() {
        if (query.indexOf('{') == -1) return;
        query = tablePrefix(query);
        debugMessageQuery("Change table name");
    }

    /**
     * Ищет в строке названия таблиц в {} и меняет их на имя таблицы с префиксом в кавычках
     *  //SELECT * FROM pfx_table_name
     *  $AgtySQL.tablePrefix("SELECT * FROM {table_name}");
     *
     * @param query String query
     * @return String
     */
    final public String tablePrefixNoQuote(String query) {
        return query.replaceAll(
                "\\{([a-zA-Z0-9_]+)}",
                getPrefix() + "$1"
        );
    }

    /**
     * This query table prefix without quotes
     */
    private void tablePrefixNoQuote() {
        if (query.indexOf('{') == -1) return;
        query = tablePrefixNoQuote(query);
        debugMessageQuery("Change table name (no quote)");
    }

    /**
     * Ищет в строке названия полей в [] и меняет их на имя поля в кавычках
     *  //SELECT * FROM `pfx_table_name` WHERE `id` = 1
     *  $AgtySQL.columnsQuote("SELECT * FROM `pfx_table_name` WHERE [id] = 1");
     *
     * @param query String query
     * @return String
     */
    final public String columnsQuote(String query) {
        return query.replaceAll(
                "\\[([a-zA-Z0-9_\\s]+)]",
                getQuoteColumn()
                        + "$1"
                        + getQuoteColumn()
        );
    }

    /**
     * This query columns quote
     */
    private void columnsQuote() {
        if (query.indexOf('[') == -1) return;
        query = columnsQuote(query);
        debugMessageQuery("Change columns");
    }
}
