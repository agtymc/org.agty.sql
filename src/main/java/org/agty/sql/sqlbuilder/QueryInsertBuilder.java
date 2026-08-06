package org.agty.sql.sqlbuilder;

import java.util.LinkedList;

public final class QueryInsertBuilder {
    private String table;
    private String columns = "";
    private LinkedList<String> values = new LinkedList<>();

    public QueryInsertBuilder setTable(String table) {
        this.table = table;
        return this;
    }

    public QueryInsertBuilder setFields(String fields) {
        columns = fields;
        return this;
    }

    public QueryInsertBuilder setValues(LinkedList<String> values) {
        this.values = values;
        return this;
    }

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
