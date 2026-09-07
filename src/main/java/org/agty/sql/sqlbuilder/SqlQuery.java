package org.agty.sql.sqlbuilder;

import org.agty.sql.data.Arguments;

/**
 * Internal base class for dialect query builders.
 *
 * @param <T> fluent self type
 */
public class SqlQuery<T> {
    /** Creates a new SqlQuery instance. */
    public SqlQuery() {
    }

    private Arguments arguments;
    private String primaryKey;
    private String quoteTable = "";
    private String quoteColumn = "";
    private String quoteValue = "";

    protected String getPrimaryKey() {
        return primaryKey;
    }

    /**
     * Sets the primary key.
     * @param primaryKey parameter value
     * @return operation result
     */
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

    /**
     * Returns the quote table.
     * @return operation result
     */
    public String getQuoteTable() {
        return quoteTable;
    }

    /**
     * Sets the quote table.
     * @param quoteTable parameter value
     * @return operation result
     */
    @SuppressWarnings("unchecked")
    public T setQuoteTable(String quoteTable) {
        this.quoteTable = quoteTable;
        return (T) this;
    }

    /**
     * Returns the quote column.
     * @return operation result
     */
    public String getQuoteColumn() {
        return quoteColumn;
    }

    /**
     * Sets the quote column.
     * @param quoteColumn parameter value
     * @return operation result
     */
    @SuppressWarnings("unchecked")
    public T setQuoteColumn(String quoteColumn) {
        this.quoteColumn = quoteColumn;
        return (T) this;
    }

    /**
     * Returns the quote value.
     * @return operation result
     */
    public String getQuoteValue() {
        return quoteValue;
    }

    /**
     * Sets the quote value.
     * @param quoteValue parameter value
     * @return operation result
     */
    @SuppressWarnings("unchecked")
    public T setQuoteValue(String quoteValue) {
        this.quoteValue = quoteValue;
        return (T) this;
    }

    protected String getWhere() {
        return getArguments().hasWhere() ? " WHERE " + getArguments().getWhere() : "";
    }
}
