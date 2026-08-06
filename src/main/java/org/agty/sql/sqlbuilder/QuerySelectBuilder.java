package org.agty.sql.sqlbuilder;

import java.util.ArrayList;
import java.util.List;

public final class QuerySelectBuilder {
    private String table;
    private final List<String> columns = new ArrayList<>();
    private final List<String> joins = new ArrayList<>();
    private String where;
    private String groupBy;
    private String having;
    private String orderBy;
    private Integer limit;
    private Integer offset;

    public QuerySelectBuilder addField(String field) {
        columns.add(field);
        return this;
    }

    public QuerySelectBuilder addJoin(String join) {
        joins.add(join);
        return this;
    }

    public QuerySelectBuilder setTable(String table) {
        this.table = table;
        return this;
    }

    public QuerySelectBuilder setWhere(String where) {
        this.where = where;
        return this;
    }

    public QuerySelectBuilder setGroupBy(String groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    public QuerySelectBuilder setHaving(String having) {
        this.having = having;
        return this;
    }

    public QuerySelectBuilder setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public QuerySelectBuilder setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public QuerySelectBuilder setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public String build() {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append(String.join(",", columns));
        query.append(" FROM ");
        query.append(table);
        query.append(" ");
        query.append(String.join(" ", joins));

        if (where != null && !where.isEmpty()) {
            query.append(" WHERE ");
            query.append(where);
        }
        if (groupBy != null && !groupBy.isEmpty()) {
            query.append(" GROUP BY ");
            query.append(groupBy);
        }
        if (having != null && !having.isEmpty()) {
            query.append(" HAVING ");
            query.append(having);
        }
        if (orderBy != null && !orderBy.isEmpty()) {
            query.append(" ORDER BY ");
            query.append(orderBy);
        }
        if (limit != null && limit > 0) {
            query.append(" LIMIT ");
            query.append(limit);
        }
        if (offset != null && offset > 0) {
            query.append(" OFFSET ");
            query.append(offset);
        }

        return query.toString();
    }
}
