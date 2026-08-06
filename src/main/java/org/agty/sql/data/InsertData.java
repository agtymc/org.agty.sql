package org.agty.sql.data;

import java.util.LinkedList;

/**
 * A query for insert data
 */
public class InsertData {
    private Arguments arguments;
    private String fields;
    private final LinkedList<String> values = new LinkedList<>();
    private String database;
    private String schema;


    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public LinkedList<String> getValues() {
        return values;
    }

    public void setValue(String value) {
        this.values.add(value);
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public Arguments getArguments() {
        return arguments;
    }

    public void setArguments(Arguments arguments) {
        this.arguments = arguments;
    }

    public boolean hasDataSet() {
        return !getFields().isEmpty();
    }
}
