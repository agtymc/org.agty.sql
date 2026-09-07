package org.agty.sql.support;

import org.agty.sql.base.RowData;
import org.agty.sql.base.RowDataEmpty;
import org.agty.sql.interfaces.SqlRow;

/**
 * Internal row factory used by current production code.
 */
public final class RowFactory {

    private RowFactory() {
    }

    /**
     * Performs the new sql row operation.
     * @return operation result
     */
    public static SqlRow newSqlRow() {
        return new RowData();
    }

    /**
     * Performs the empty row operation.
     * @return operation result
     */
    public static SqlRow emptyRow() {
        return new RowDataEmpty();
    }
}
