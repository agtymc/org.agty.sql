package org.agty.sql.sqlbuilder;

public final class QueryUpdateBuilder {
    private String table;
    private String updateData;
    private String where;
    private String order;
    private Integer limit;
    private Integer offset;

    public QueryUpdateBuilder setTable(String table) {
        this.table = table;
        return this;
    }

    public QueryUpdateBuilder setUpdateData(String updateData) {
        this.updateData = updateData;
        return this;
    }

    public QueryUpdateBuilder setWhere(String where) {
        this.where = where;
        return this;
    }

    public QueryUpdateBuilder setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public QueryUpdateBuilder setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public QueryUpdateBuilder setOrderBy(String order) {
        this.order = order;
        return this;
    }

    public String build() {
        StringBuilder query = new StringBuilder();
        query.append("UPDATE ");
        query.append(table);
        query.append(" SET ");
        query.append(updateData);

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
