package org.agty.sql.sqlbuilder;

import org.agty.sql.support.SqlTextUtils;

/**
 * Internal builder for key=value or bare value SQL fragments.
 */
public final class QueryValueDataBuilder {
    private String quoteColumn;
    private String quoteValue;
    private String column;
    private Object value;
    private boolean noStringEncode;

    public QueryValueDataBuilder setQuoteColumn(String quoteColumn) {
        this.quoteColumn = quoteColumn;
        return this;
    }

    public QueryValueDataBuilder setQuoteValue(String quoteValue) {
        this.quoteValue = quoteValue;
        return this;
    }

    public QueryValueDataBuilder setColumn(String column) {
        this.column = column;
        return this;
    }

    public QueryValueDataBuilder setValue(Object value) {
        this.value = value;
        return this;
    }

    public QueryValueDataBuilder setNoStringEncode(boolean noStringEncode) {
        this.noStringEncode = noStringEncode;
        return this;
    }

    public String build() {
        StringBuilder query = new StringBuilder();

        if (column != null) {
            query.append(quoteColumn);
            query.append(column);
            query.append(quoteColumn);
            query.append('=');
        }

        if (value == null || value.toString().isEmpty()) {
            query.append("NULL");
        } else if (value instanceof Integer val) {
            query.append(val);
        } else if (value instanceof Long val) {
            query.append(val);
        } else if (value instanceof Short val) {
            query.append(val);
        } else if (value instanceof Boolean val) {
            query.append(val);
        } else if (value.toString().startsWith("[~")) {
            query.append(dataEncode(value.toString(), noStringEncode), 2, value.toString().length());
        } else {
            query.append(quoteValue);
            query.append(dataEncode(value.toString(), noStringEncode));
            query.append(quoteValue);
        }

        return query.toString();
    }

    private String dataEncode(String data, boolean noEncode) {
        return noEncode ? data : SqlTextUtils.hencode(data);
    }
}
