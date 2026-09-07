package org.agty.sql.sqlbuilder;

import org.agty.sql.support.SqlTextUtils;

/**
 * Renders a value or column-value pair for legacy SQL and prepared statements.
 */
public final class SqlValueRenderer {
    /** Creates a new SqlValueRenderer instance. */
    public SqlValueRenderer() {
    }

    private String quoteColumn;
    private String quoteValue;
    private String column;
    private Object value;
    private boolean noStringEncode;
    private boolean statementPrepare;

    /**
     * Sets the quote column.
     * @param quoteColumn parameter value
     * @return operation result
     */
    public SqlValueRenderer setQuoteColumn(String quoteColumn) {
        this.quoteColumn = quoteColumn;
        return this;
    }

    /**
     * Sets the quote value.
     * @param quoteValue parameter value
     * @return operation result
     */
    public SqlValueRenderer setQuoteValue(String quoteValue) {
        this.quoteValue = quoteValue;
        return this;
    }

    /**
     * Sets the column.
     * @param column parameter value
     * @return operation result
     */
    public SqlValueRenderer setColumn(String column) {
        this.column = column;
        return this;
    }

    /**
     * Sets the value.
     * @param value parameter value
     * @return operation result
     */
    public SqlValueRenderer setValue(Object value) {
        this.value = value;
        return this;
    }

    /**
     * Sets the no string encode.
     * @param noStringEncode parameter value
     * @return operation result
     */
    public SqlValueRenderer setNoStringEncode(boolean noStringEncode) {
        this.noStringEncode = noStringEncode;
        return this;
    }

    /**
     * Performs the use statement prepare operation.
     * @param statementPrepare parameter value
     * @return operation result
     */
    public SqlValueRenderer useStatementPrepare(boolean statementPrepare) {
        this.statementPrepare = statementPrepare;
        return this;
    }

    /**
     * Performs the render operation.
     * @return operation result
     */
    public String render() {
        StringBuilder query = new StringBuilder();

        if (column != null) {
            query.append(quoteColumn);
            query.append(column);
            query.append(quoteColumn);
            query.append('=');
        }

        if (statementPrepare) {
            query.append('?');
        } else if (value == null || value.toString().isEmpty()) {
            query.append("NULL");
        } else if (value instanceof Number number) {
            query.append(number);
        } else if (value instanceof Boolean booleanValue) {
            query.append(booleanValue);
        } else if (value.toString().startsWith("[~")) {
            query.append(encode(value.toString()), 2, value.toString().length());
        } else {
            query.append(quoteValue);
            query.append(encode(value.toString()));
            query.append(quoteValue);
        }

        return query.toString();
    }

    private String encode(String data) {
        return noStringEncode ? data : SqlTextUtils.hencode(data);
    }
}
