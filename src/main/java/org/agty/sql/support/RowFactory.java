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

    public static SqlRow newSqlRow() {
        return new RowData();
    }

    public static SqlRow emptyRow() {
        return new RowDataEmpty();
    }
}
