package org.agty.sql.dialect.mysql.queries;

import org.agty.sql.sqlbuilder.SqlQuery;
import org.agty.sql.data.Arguments;
import org.agty.sql.sqlbuilder.QuerySelectBuilder;

/**
 * Dialect-local helper for order/limit aware UPDATE/DELETE subqueries.
 */
class QueryForUpdateDelete extends SqlQuery<QueryForUpdateDelete> {
    private String query;

    QueryForUpdateDelete(Arguments arguments) {
        setArguments(arguments);
    }

    String getOrderLimitWhere() {
        if (query == null) {
            query = createOrderLimitWhere();
        }
        return query;
    }

    private String createOrderLimitWhere() {
        if (getPrimaryKey() == null || getPrimaryKey().isEmpty()) {
            return null;
        }

        StringBuilder query = new StringBuilder();

        query.append(getQuoteColumn());
        query.append(getPrimaryKey());
        query.append(getQuoteColumn());

        query.append(" IN ( ");

        query.append(
                new QuerySelectBuilder()
                        .addField(getQuoteColumn() + getPrimaryKey() + getQuoteColumn())
                        .setTable(getArguments().getTable())
                        .setWhere(getArguments().getWhere())
                        .setOrderBy(getArguments().getOrderBy())
                        .setHaving(getArguments().getHaving())
                        .setGroupBy(getArguments().getGroupBy())
                        .setLimit(getArguments().getLimit())
                        .setOffset(getArguments().getOffset())
                        .build()
        );

        query.append(")");

        return query.toString();
    }
}
