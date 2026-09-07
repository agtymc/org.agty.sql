package org.agty.sql.sqlbuilder;

/**
 * Provides query update builder behavior.
 */
public final class QueryUpdateBuilder {
    /** Creates a new QueryUpdateBuilder instance. */
    public QueryUpdateBuilder() {
    }

    private String table;
    private String updateData;
    private String where;
    private String order;
    private Integer limit;
    private Integer offset;

    /**
     * Sets the table.
     * @param table parameter value
     * @return operation result
     */
    public QueryUpdateBuilder setTable(String table) {
        this.table = table;
        return this;
    }

    /**
     * Sets the update data.
     * @param updateData parameter value
     * @return operation result
     */
    public QueryUpdateBuilder setUpdateData(String updateData) {
        this.updateData = updateData;
        return this;
    }

    /**
     * Sets the where.
     * @param where parameter value
     * @return operation result
     */
    public QueryUpdateBuilder setWhere(String where) {
        this.where = where;
        return this;
    }

    /**
     * Sets the limit.
     * @param limit parameter value
     * @return operation result
     */
    public QueryUpdateBuilder setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Sets the offset.
     * @param offset parameter value
     * @return operation result
     */
    public QueryUpdateBuilder setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Sets the order by.
     * @param order parameter value
     * @return operation result
     */
    public QueryUpdateBuilder setOrderBy(String order) {
        this.order = order;
        return this;
    }

    /**
     * Performs the build operation.
     * @return operation result
     */
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
            query.append(offset);
        }

        return query.toString();
    }
}
