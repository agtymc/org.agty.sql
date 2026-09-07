package org.agty.sql.operations;

import org.agty.sql.AgtySqlOperationSupport;
import org.agty.sql.data.Arguments;
import org.agty.sql.support.PreparedStatementSupport;

/**
 * Provides delete operation behavior.
 */
public final class DeleteOperation {

    private final AgtySqlOperationSupport support;

    /**
     * Creates a new instance.
     * @param support parameter value
     */
    public DeleteOperation(AgtySqlOperationSupport support) {
        this.support = support;
    }

    /**
     * Performs the delete operation.
     * @param arguments parameter value
     * @return operation result
     */
    public boolean delete(Arguments arguments) {
        String query = support.hasQuery(arguments)
                ? arguments.getQuery()
                : support.getDriverSqlObject().deleteQuery(arguments);

        if (support.hasQuery(query)) {
            if (arguments.useStatementPrepare()) {
                support.executePreparedUpdate(
                        query,
                        PreparedStatementSupport.readParameters(arguments),
                        arguments.noRebuildQuery()
                );
            } else {
                support.execute(query, arguments.noRebuildQuery());
            }
        } else {
            support.throwError("AgtySQL.delete()", "No a query for UPDATE");
        }

        return !support.hasErrors();
    }
}
