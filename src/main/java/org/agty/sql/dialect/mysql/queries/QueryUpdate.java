package org.agty.sql.dialect.mysql.queries;

import org.agty.sql.sqlbuilder.SqlQuery;
import org.agty.sql.data.Arguments;
import org.agty.sql.interfaces.SqlQueries;
import org.agty.sql.sqlbuilder.QueryUpdateBuilder;

/**
 * A query for update
 */
public class QueryUpdate extends SqlQuery<QueryUpdate> implements SqlQueries {
    private String query;
    private String updateData;

    public QueryUpdate(Arguments arguments) {
        setArguments(arguments);
    }

    public String getUpdateData() {
        return updateData;
    }

    public boolean isUpdateData() {
        return updateData != null && !updateData.isEmpty();
    }

    public QueryUpdate setUpdateData(String updateData) {
        this.updateData = updateData;
        return this;
    }

    private String createQuery() {
        if (getArguments().hasGroupBy()) {
            return new QueryUpdateBuilder()
                    .setTable(getArguments().getTable())
                    .setUpdateData(getUpdateData())
                    .setWhere(getQueryForUpdateDelete())
                    .build();
        }

        return new QueryUpdateBuilder()
                .setTable(getArguments().getTable())
                .setUpdateData(getUpdateData())
                .setWhere(getArguments().getWhere())
                .setLimit(getArguments().getLimit())
                .setOffset(getArguments().getOffset())
                .setOrderBy(getArguments().getOrderBy())
                .build();
    }

    private String getQueryForUpdateDelete() {
        QueryForUpdateDelete pgSqlQuery = new QueryForUpdateDelete(getArguments());
        pgSqlQuery.setPrimaryKey(getPrimaryKey());
        pgSqlQuery.setQuoteColumn(getQuoteColumn());
        pgSqlQuery.setQuoteTable(getQuoteTable());
        pgSqlQuery.setQuoteValue(getQuoteValue());
        return pgSqlQuery.getOrderLimitWhere();
    }

    @Override
    public String getQuery() {
        if (!getArguments().hasTable() || !isUpdateData()) return null;

        if (query == null) {
            query = createQuery();
        }
        return query;
    }
}
