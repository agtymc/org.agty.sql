package org.agty.sql.sqlbuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides query select builder behavior.
 */
public final class QuerySelectBuilder {
    /** Creates a new QuerySelectBuilder instance. */
    public QuerySelectBuilder() {
    }

    private String table;
    private final List<String> columns = new ArrayList<>();
    private final List<String> joins = new ArrayList<>();
    private String where;
    private String groupBy;
    private String having;
    private String orderBy;
    private Integer limit;
    private Integer offset;

    /**
     * Adds the field.
     * @param field parameter value
     * @return operation result
     */
    public QuerySelectBuilder addField(String field) {
        columns.add(field);
        return this;
    }

    /**
     * Adds the join.
     * @param join parameter value
     * @return operation result
     */
    public QuerySelectBuilder addJoin(String join) {
        joins.add(join);
        return this;
    }

    /**
     * Sets the table.
     * @param table parameter value
     * @return operation result
     */
    public QuerySelectBuilder setTable(String table) {
        this.table = table;
        return this;
    }

    /**
     * Sets the where.
     * @param where parameter value
     * @return operation result
     */
    public QuerySelectBuilder setWhere(String where) {
        this.where = where;
        return this;
    }

    /**
     * Sets the group by.
     * @param groupBy parameter value
     * @return operation result
     */
    public QuerySelectBuilder setGroupBy(String groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    /**
     * Sets the having.
     * @param having parameter value
     * @return operation result
     */
    public QuerySelectBuilder setHaving(String having) {
        this.having = having;
        return this;
    }

    /**
     * Sets the order by.
     * @param orderBy parameter value
     * @return operation result
     */
    public QuerySelectBuilder setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    /**
     * Sets the limit.
     * @param limit parameter value
     * @return operation result
     */
    public QuerySelectBuilder setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Sets the offset.
     * @param offset parameter value
     * @return operation result
     */
    public QuerySelectBuilder setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Performs the build operation.
     * @return operation result
     */
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
