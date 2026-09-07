package org.agty.sql.dialect.mysql.queries;

import org.agty.sql.sqlbuilder.SqlQuery;
import org.agty.sql.data.Arguments;
import org.agty.sql.interfaces.SqlQueries;
import org.agty.sql.sqlbuilder.QuerySelectBuilder;

/**
 * A query for select a max value or a min value
 */
public class QueryMinMax extends SqlQuery<QueryMinMax> implements SqlQueries {
    private String query;

    private boolean getMax = false;

    /**
     * Creates a new instance.
     * @param arguments parameter value
     */
    public QueryMinMax(Arguments arguments) {
        setArguments(arguments);
    }

    /**
     * Returns the max.
     * @return operation result
     */
    public QueryMinMax getMax() {
        this.getMax = true;
        return this;
    }

    /**
     * Returns the min.
     * @return operation result
     */
    public QueryMinMax getMin() {
        this.getMax = false;
        return this;
    }

    private String createQuery() {
        String function = getMax ? "MAX" : "MIN";

        return new QuerySelectBuilder()
                .addField(function + "(" + getArguments().getActionField() + ") as \"M\"")
                .setTable(getArguments().getTable())
                .setWhere(getArguments().getWhere())
                .setGroupBy(getArguments().getGroupBy())
                .setHaving(getArguments().getHaving())
                .setOrderBy(getArguments().getOrderBy())
                .setLimit(getArguments().getLimit())
                .setOffset(getArguments().getOffset())
                .build();
    }

    @Override
    public String getQuery() {
        if (!getArguments().hasTable() || !getArguments().hasActionField()) return null;

        if (query == null) {
            query = createQuery();
        }
        return query;
    }
}
