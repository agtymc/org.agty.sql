package org.agty.sql.dialect.mysql.queries;

import org.agty.sql.sqlbuilder.SqlQuery;
import org.agty.sql.data.Arguments;
import org.agty.sql.interfaces.SqlQueries;
import org.agty.sql.sqlbuilder.QuerySelectBuilder;

/**
 * A query for select a first row or a last row
 */
public class QueryFirstLast extends SqlQuery<QueryFirstLast> implements SqlQueries {
    private String createdQuery;

    private boolean getLast = false;

    /**
     * Creates a new instance.
     * @param arguments parameter value
     */
    public QueryFirstLast(Arguments arguments) {
        setArguments(arguments);
    }

    /**
     * Returns the last.
     * @return operation result
     */
    public QueryFirstLast getLast() {
        this.getLast = true;
        return this;
    }

    /**
     * Returns the first.
     * @return operation result
     */
    public QueryFirstLast getFirst() {
        this.getLast = false;
        return this;
    }

    private String createQuery() {
        String order = getLast ? " DESC" : " ASC";

        return new QuerySelectBuilder()
                .addField(getArguments().getFields())
                .setTable(getArguments().getTable())
                .setWhere(getArguments().getWhere())
                .setGroupBy(getArguments().getGroupBy())
                .setHaving(getArguments().getHaving())
                .setOrderBy(getArguments().getActionField() + order)
                .setLimit(1)
                .build();
    }

    @Override
    public String getQuery() {
        if (!getArguments().hasTable() || !getArguments().hasActionField()) return null;

        if (createdQuery == null) {
            createdQuery = createQuery();
        }
        return createdQuery;
    }
}
