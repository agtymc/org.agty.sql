package org.agty.sql.sqlbuilder;

public final class QueryDeleteBuilder {
    private String table;
    private String where;
    private String order;
    private Integer limit;
    private Integer offset;

    public QueryDeleteBuilder setTable(String table) {
        this.table = table;
        return this;
    }

    public QueryDeleteBuilder setWhere(String where) {
        this.where = where;
        return this;
    }

    public QueryDeleteBuilder setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public QueryDeleteBuilder setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public QueryDeleteBuilder setOrderBy(String order) {
        this.order = order;
        return this;
    }

    public String build() {
        StringBuilder query = new StringBuilder();
        query.append("DELETE FROM ");
        query.append(table);

        if (where != null && !where.isEmpty()) {
            query.append(" WHERE ");
            query.append(where);
        }
        if (order != null && !order.isEmpty()) {
            query.append(" ORDER BY ");
            query.append(order);
        }
        if (limit != null && limit > 0) {
            query.append(" LIMIT ");
            query.append(limit);
        }
        if (offset != null && offset > 0) {
            query.append(" OFFSET ");
            query.append(limit);
        }

        return query.toString();
    }
}
