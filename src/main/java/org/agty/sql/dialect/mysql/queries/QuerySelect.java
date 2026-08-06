package org.agty.sql.dialect.mysql.queries;

import org.agty.sql.sqlbuilder.SqlQuery;
import org.agty.sql.data.Arguments;
import org.agty.sql.interfaces.SqlQueries;
import org.agty.sql.sqlbuilder.QuerySelectBuilder;

/**
 * An query for select
 */
public class QuerySelect extends SqlQuery<QuerySelect> implements SqlQueries {
    private String query;

    public QuerySelect(Arguments arguments) {
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
                .setLimit(getArguments().getLimit())
                .setOffset(getArguments().getOffset())
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
