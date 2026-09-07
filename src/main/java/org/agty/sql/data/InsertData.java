package org.agty.sql.data;

import java.util.LinkedList;

/**
 * A query for insert data
 */
public class InsertData {
    /** Creates a new InsertData instance. */
    public InsertData() {
    }

    private Arguments arguments;
    private String fields;
    private final LinkedList<String> values = new LinkedList<>();
    private String database;
    private String schema;


    /**
     * Returns the fields.
     * @return operation result
     */
    public String getFields() {
        return fields;
    }

    /**
     * Sets the fields.
     * @param fields parameter value
     */
    public void setFields(String fields) {
        this.fields = fields;
    }

    /**
     * Returns the values.
     * @return operation result
     */
    public LinkedList<String> getValues() {
        return values;
    }

    /**
     * Sets the value.
     * @param value parameter value
     */
    public void setValue(String value) {
        this.values.add(value);
    }

    /**
     * Returns the database.
     * @return operation result
     */
    public String getDatabase() {
        return database;
    }

    /**
     * Sets the database.
     * @param database parameter value
     */
    public void setDatabase(String database) {
        this.database = database;
    }

    /**
     * Returns the schema.
     * @return operation result
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Sets the schema.
     * @param schema parameter value
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * Returns the arguments.
     * @return operation result
     */
    public Arguments getArguments() {
        return arguments;
    }

    /**
     * Sets the arguments.
     * @param arguments parameter value
     */
    public void setArguments(Arguments arguments) {
        this.arguments = arguments;
    }

    /**
     * Returns whether data set.
     * @return operation result
     */
    public boolean hasDataSet() {
        return !getFields().isEmpty();
    }
}
