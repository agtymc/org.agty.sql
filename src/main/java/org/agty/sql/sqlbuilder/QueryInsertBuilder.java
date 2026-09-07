package org.agty.sql.sqlbuilder;

import java.util.LinkedList;

/**
 * Provides query insert builder behavior.
 */
public final class QueryInsertBuilder {
    /** Creates a new QueryInsertBuilder instance. */
    public QueryInsertBuilder() {
    }

    private String table;
    private String columns = "";
    private LinkedList<String> values = new LinkedList<>();

    /**
     * Sets the table.
     * @param table parameter value
     * @return operation result
     */
    public QueryInsertBuilder setTable(String table) {
        this.table = table;
        return this;
    }

    /**
     * Sets the fields.
     * @param fields parameter value
     * @return operation result
     */
    public QueryInsertBuilder setFields(String fields) {
        columns = fields;
        return this;
    }

    /**
     * Sets the values.
     * @param values parameter value
     * @return operation result
     */
    public QueryInsertBuilder setValues(LinkedList<String> values) {
        this.values = values;
        return this;
    }

    /**
     * Performs the build operation.
     * @return operation result
     */
    public String build() {
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO ");
        query.append(table);
        query.append(" (");
        query.append(columns);
        query.append(") VALUES ");

        for (String value : values) {
            query.append("(");
            query.append(value);
            query.append("),");
        }

        query.setLength(query.length() - 1);
        return query.toString();
    }
}
