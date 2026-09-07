package org.agty.sql.sqlbuilder;

/**
 * Provides query delete builder behavior.
 */
public final class QueryDeleteBuilder {
    /** Creates a new QueryDeleteBuilder instance. */
    public QueryDeleteBuilder() {
    }

    private String table;
    private String where;
    private String order;
    private Integer limit;
    private Integer offset;

    /**
     * Sets the table.
     * @param table parameter value
     * @return operation result
     */
    public QueryDeleteBuilder setTable(String table) {
        this.table = table;
        return this;
    }

    /**
     * Sets the where.
     * @param where parameter value
     * @return operation result
     */
    public QueryDeleteBuilder setWhere(String where) {
        this.where = where;
        return this;
    }

    /**
     * Sets the limit.
     * @param limit parameter value
     * @return operation result
     */
    public QueryDeleteBuilder setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Sets the offset.
     * @param offset parameter value
     * @return operation result
     */
    public QueryDeleteBuilder setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Sets the order by.
     * @param order parameter value
     * @return operation result
     */
    public QueryDeleteBuilder setOrderBy(String order) {
        this.order = order;
        return this;
    }

    /**
     * Performs the build operation.
     * @return operation result
     */
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
            query.append(offset);
        }

        return query.toString();
    }
}
