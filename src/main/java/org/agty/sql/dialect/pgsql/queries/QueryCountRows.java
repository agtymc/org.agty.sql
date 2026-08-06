package org.agty.sql.dialect.pgsql.queries;

import org.agty.sql.sqlbuilder.SqlQuery;
import org.agty.sql.data.Arguments;
import org.agty.sql.interfaces.SqlQueries;
import org.agty.sql.sqlbuilder.QuerySelectBuilder;

/**
 * A query for count rows
 */
public class QueryCountRows extends SqlQuery<QueryCountRows> implements SqlQueries {
    private String query;

    public QueryCountRows(Arguments arguments) {
        setArguments(arguments);
    }

    private String createQuery() {
        return new QuerySelectBuilder()
                .addField("COUNT(*) as \"rows\"")
                .setTable(getArguments().getTable())
                .setWhere(getArguments().getWhere())
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
