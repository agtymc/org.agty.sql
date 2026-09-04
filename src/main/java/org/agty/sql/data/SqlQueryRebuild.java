package org.agty.sql.data;

import org.agty.sql.support.DebugMessages;

/**
 * Rebuild a query
 */
public class SqlQueryRebuild {
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
        this.query = query;
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
        if (query == null || query.isEmpty()) return query;

        String originalQuery = query;
        query = rebuildOutsideProtectedSql(query);
        setQueryIsChanged(!originalQuery.equals(query));
        debugMessageQuery("Rebuilt query");
        return query;
    }

    /**
     * Debug messages
     * @param message Message
     */
    private void debugMessageQuery(String message) {
        if (isDebug()) DebugMessages.print("SqlQueryRebuild()", message + ": " + query);
    }

    private String rebuildOutsideProtectedSql(String source) {
        StringBuilder result = new StringBuilder(source.length());

        for (int index = 0; index < source.length();) {
            char current = source.charAt(index);

            if (current == '\'' || current == '"' || current == '`') {
                index = appendQuoted(source, result, index, current);
                continue;
            }

            if (current == '-' && hasNext(source, index, '-')) {
                index = appendLineComment(source, result, index);
                continue;
            }

            if (current == '/' && hasNext(source, index, '*')) {
                index = appendBlockComment(source, result, index);
                continue;
            }

            if (current == '$') {
                int nextIndex = appendDollarQuoted(source, result, index);
                if (nextIndex != index) {
                    index = nextIndex;
                    continue;
                }
            }

            if (current == '{') {
                int end = source.indexOf('}', index + 1);
                if (end > index && isTableIdentifier(source, index + 1, end)) {
                    result.append(quoteTable).append(prefix);
                    result.append(source, index + 1, end);
                    result.append(quoteTable);
                    index = end + 1;
                    continue;
                }
            }

            if (current == '[') {
                int end = source.indexOf(']', index + 1);
                if (end > index && isColumnIdentifier(source, index + 1, end)) {
                    result.append(quoteColumn);
                    result.append(source, index + 1, end);
                    result.append(quoteColumn);
                    index = end + 1;
                    continue;
                }
            }

            result.append(current);
            index++;
        }

        return result.toString();
    }

    private int appendQuoted(String source, StringBuilder result, int start, char quote) {
        int index = start;
        result.append(source.charAt(index++));

        while (index < source.length()) {
            char current = source.charAt(index);
            result.append(current);
            index++;

            if (current == '\\' && index < source.length()) {
                result.append(source.charAt(index++));
                continue;
            }

            if (current != quote) continue;
            if (index < source.length() && source.charAt(index) == quote) {
                result.append(source.charAt(index++));
                continue;
            }
            break;
        }

        return index;
    }

    private int appendLineComment(String source, StringBuilder result, int start) {
        int index = start;
        while (index < source.length()) {
            char current = source.charAt(index++);
            result.append(current);
            if (current == '\n' || current == '\r') break;
        }
        return index;
    }

    private int appendBlockComment(String source, StringBuilder result, int start) {
        int index = start;
        while (index < source.length()) {
            char current = source.charAt(index++);
            result.append(current);
            if (current == '*' && index < source.length() && source.charAt(index) == '/') {
                result.append(source.charAt(index++));
                break;
            }
        }
        return index;
    }

    private int appendDollarQuoted(String source, StringBuilder result, int start) {
        int delimiterEnd = source.indexOf('$', start + 1);
        if (delimiterEnd < 0 || !isDollarTag(source, start + 1, delimiterEnd)) return start;

        String delimiter = source.substring(start, delimiterEnd + 1);
        int contentEnd = source.indexOf(delimiter, delimiterEnd + 1);
        if (contentEnd < 0) {
            result.append(source, start, source.length());
            return source.length();
        }

        int end = contentEnd + delimiter.length();
        result.append(source, start, end);
        return end;
    }

    private boolean isDollarTag(String source, int start, int end) {
        if (start == end) return true;
        char first = source.charAt(start);
        if (!isAsciiLetter(first) && first != '_') return false;
        for (int index = start + 1; index < end; index++) {
            char current = source.charAt(index);
            if (!isAsciiLetter(current) && !Character.isDigit(current) && current != '_') return false;
        }
        return true;
    }

    private boolean isTableIdentifier(String source, int start, int end) {
        if (start == end) return false;
        for (int index = start; index < end; index++) {
            char current = source.charAt(index);
            if (!isAsciiLetter(current) && !Character.isDigit(current) && current != '_') return false;
        }
        return true;
    }

    private boolean isColumnIdentifier(String source, int start, int end) {
        if (start == end) return false;
        for (int index = start; index < end; index++) {
            char current = source.charAt(index);
            if (!isAsciiLetter(current)
                    && !Character.isDigit(current)
                    && current != '_'
                    && !Character.isWhitespace(current)) {
                return false;
            }
        }
        return true;
    }

    private boolean isAsciiLetter(char value) {
        return value >= 'a' && value <= 'z' || value >= 'A' && value <= 'Z';
    }

    private boolean hasNext(String source, int index, char expected) {
        return index + 1 < source.length() && source.charAt(index + 1) == expected;
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

}
