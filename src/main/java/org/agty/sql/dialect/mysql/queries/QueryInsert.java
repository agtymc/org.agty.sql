package org.agty.sql.dialect.mysql.queries;

import org.agty.sql.sqlbuilder.SqlQuery;
import org.agty.sql.data.Arguments;
import org.agty.sql.data.InsertData;
import org.agty.sql.interfaces.SqlQueries;
import org.agty.sql.sqlbuilder.QueryInsertBuilder;

/**
 * A query for insert
 */
public class QueryInsert extends SqlQuery<QueryInsert> implements SqlQueries {
    private String createdQuery;

    private InsertData insertData;

    public QueryInsert(Arguments arguments) {
        setArguments(arguments);
    }

    public QueryInsert setInsertData(InsertData insertData) {
        this.insertData = insertData;
        return this;
    }

    public InsertData getInsertData() {
        return insertData;
    }

    private String createQuery() {
        return new QueryInsertBuilder()
                .setTable(getArguments().getTable())
                .setFields(getInsertData().getFields())
                .setValues(getInsertData().getValues())
                .build();
    }

    @Override
    public String getQuery() {
        if (!getArguments().hasTable() || getInsertData() == null || !getInsertData().hasDataSet()) return null;

        if (createdQuery == null) {
            createdQuery = createQuery();
        }
        return createdQuery;
    }
}
