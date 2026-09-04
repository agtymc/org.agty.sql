package org.agty.sql.sqlbuilder;

import org.agty.sql.support.SqlTextUtils;

/**
 * Renders a value or column-value pair for legacy SQL and prepared statements.
 */
public final class SqlValueRenderer {
    private String quoteColumn;
    private String quoteValue;
    private String column;
    private Object value;
    private boolean noStringEncode;
    private boolean statementPrepare;

    public SqlValueRenderer setQuoteColumn(String quoteColumn) {
        this.quoteColumn = quoteColumn;
        return this;
    }

    public SqlValueRenderer setQuoteValue(String quoteValue) {
        this.quoteValue = quoteValue;
        return this;
    }

    public SqlValueRenderer setColumn(String column) {
        this.column = column;
        return this;
    }

    public SqlValueRenderer setValue(Object value) {
        this.value = value;
        return this;
    }

    public SqlValueRenderer setNoStringEncode(boolean noStringEncode) {
        this.noStringEncode = noStringEncode;
        return this;
    }

    public SqlValueRenderer useStatementPrepare(boolean statementPrepare) {
        this.statementPrepare = statementPrepare;
        return this;
    }

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
