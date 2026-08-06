package org.agty.sql.dialect.mariadb;

import org.agty.sql.AgtySQL;
import org.agty.sql.driver.DialectCapabilities;
import org.agty.sql.driver.LastInsertIdStrategy;
import org.agty.sql.driver.UpdateAndGetStrategy;
import org.agty.sql.driver.WriteReturnStrategy;
import org.agty.sql.dialect.mysql.MySQL;

public class MariaDB extends MySQL {

    private static final String DRIVER = "mariadb";

    public MariaDB(AgtySQL agtySQL) {
        super(agtySQL);
    }

    @Override
    public String getDriverName() {
        return DRIVER;
    }

    @Override
    public DialectCapabilities getCapabilities() {
        return DialectCapabilities.of(
                false,
                false,
                LastInsertIdStrategy.CONNECTION_FUNCTION,
                WriteReturnStrategy.FOLLOW_UP_FETCH,
                UpdateAndGetStrategy.FOLLOW_UP_FETCH_BY_WHERE
        );
    }
}
