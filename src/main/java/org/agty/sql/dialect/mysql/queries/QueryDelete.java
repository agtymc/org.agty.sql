package org.agty.sql.dialect.mysql.queries;

import org.agty.sql.sqlbuilder.SqlQuery;
import org.agty.sql.data.Arguments;
import org.agty.sql.interfaces.SqlQueries;
import org.agty.sql.sqlbuilder.QueryDeleteBuilder;

/**
 * A query for delete
 */
public class QueryDelete extends SqlQuery<QueryDelete> implements SqlQueries {
    private String query;

    public QueryDelete(Arguments arguments) {
        setArguments(arguments);
    }

    private String createQuery() {
        if (getArguments().hasGroupBy()) {
            return new QueryDeleteBuilder()
                    .setTable(getArguments().getTable())
                    .setWhere(getQueryIfHasGroupBy())
                    .build();
        }

        return new QueryDeleteBuilder()
                .setTable(getArguments().getTable())
                .setWhere(getArguments().getWhere())
                .setLimit(getArguments().getLimit())
                .setOffset(getArguments().getOffset())
                .setOrderBy(getArguments().getOrderBy())
                .build();
    }

    /**
     * If it uses GroupBy condition the need subquery for this
     * Need for PgSQL driver
     * @return subquery
     */
    private String getQueryIfHasGroupBy() {
        QueryForUpdateDelete query = new QueryForUpdateDelete(getArguments());

        query.setPrimaryKey(getPrimaryKey());
        query.setQuoteColumn(getQuoteColumn());
        query.setQuoteTable(getQuoteTable());
        query.setQuoteValue(getQuoteValue());

        return query.getOrderLimitWhere();
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
