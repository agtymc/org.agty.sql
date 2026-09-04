package org.agty.sql.sqlbuilder;

import org.agty.sql.data.Arguments;

/**
 * Internal base class for dialect query builders.
 *
 * @param <T> fluent self type
 */
public class SqlQuery<T> {
    private Arguments arguments;
    private String primaryKey;
    private String quoteTable = "";
    private String quoteColumn = "";
    private String quoteValue = "";

    protected String getPrimaryKey() {
        return primaryKey;
    }

    @SuppressWarnings("unchecked")
    public T setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
        return (T) this;
    }

    protected final void setArguments(Arguments arguments) {
        if (!hasArguments()) {
            this.arguments = arguments;
        }
    }

    protected boolean hasArguments() {
        return arguments != null;
    }

    protected Arguments getArguments() {
        return arguments;
    }

    public String getQuoteTable() {
        return quoteTable;
    }

    @SuppressWarnings("unchecked")
    public T setQuoteTable(String quoteTable) {
        this.quoteTable = quoteTable;
        return (T) this;
    }

    public String getQuoteColumn() {
        return quoteColumn;
    }

    @SuppressWarnings("unchecked")
    public T setQuoteColumn(String quoteColumn) {
        this.quoteColumn = quoteColumn;
        return (T) this;
    }

    public String getQuoteValue() {
        return quoteValue;
    }

    @SuppressWarnings("unchecked")
    public T setQuoteValue(String quoteValue) {
        this.quoteValue = quoteValue;
        return (T) this;
    }

    protected String getWhere() {
        return getArguments().hasWhere() ? " WHERE " + getArguments().getWhere() : "";
    }
}
