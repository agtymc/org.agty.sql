package org.agty.sql.dialect.pgsql.queries;

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
        return new QueryDeleteBuilder()
                .setTable(getArguments().getTable())
                .setWhere(hasOrderOrLimit() ? getQueryIfOrderOrLimit() : getArguments().getWhere())
                .build();
    }

    private boolean hasOrderOrLimit() {
        return getArguments().hasOneOfOrderGroupHaving() || getArguments().hasLimitOrOffset();
    }

    /**
     * If it uses Order Group or Limit condition the need subquery for this
     * Need for PgSQL driver
     * @return subquery
     */
    private String getQueryIfOrderOrLimit() {
        QueryForUpdateDelete pgSqlQuery = new QueryForUpdateDelete(getArguments());

        pgSqlQuery.setPrimaryKey(getPrimaryKey());
        pgSqlQuery.setQuoteColumn(getQuoteColumn());
        pgSqlQuery.setQuoteTable(getQuoteTable());
        pgSqlQuery.setQuoteValue(getQuoteValue());

        return pgSqlQuery.getOrderLimitWhere();
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
