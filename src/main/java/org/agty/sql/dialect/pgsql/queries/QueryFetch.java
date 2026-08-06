package org.agty.sql.dialect.pgsql.queries;

import org.agty.sql.sqlbuilder.SqlQuery;
import org.agty.sql.interfaces.SqlQueries;
import org.agty.sql.data.Arguments;
import org.agty.sql.sqlbuilder.QuerySelectBuilder;

/**
 * A query for fetch row
 */
public class QueryFetch extends SqlQuery<QueryFetch> implements SqlQueries {
    private String query;

    public QueryFetch(Arguments arguments) {
        setArguments(arguments);
    }

    private String createQuery() {
       return new QuerySelectBuilder()
                .addField(getArguments().getFields())
                .setTable(getArguments().getTable())
                .setWhere(getArguments().getWhere())
                .setGroupBy(getArguments().getGroupBy())
                .setHaving(getArguments().getHaving())
                .setOrderBy(getArguments().getOrderBy())
                .setLimit(1)
                .build();
    }

    @Override
    public String getQuery() {
        if (!getArguments().hasTable()) return null;

        if (query == null) {
            query = createQuery();
        }
        return query;
    }
}
