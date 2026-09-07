package org.agty.sql.dialect.mariadb;

import org.agty.sql.AgtySQL;
import org.agty.sql.driver.DialectCapabilities;
import org.agty.sql.driver.DialectFeature;
import org.agty.sql.driver.DialectFeatureSet;
import org.agty.sql.driver.DialectFeatureSupport;
import org.agty.sql.driver.LastInsertIdStrategy;
import org.agty.sql.driver.UpdateAndGetStrategy;
import org.agty.sql.driver.WriteReturnStrategy;
import org.agty.sql.dialect.mysql.MySQL;

/**
 * Provides maria db behavior.
 */
public class MariaDB extends MySQL {

    private static final String DRIVER = "mariadb";

    /**
     * Creates a new instance.
     * @param agtySQL parameter value
     */
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

    @Override
    public DialectFeatureSet getFeatureSet() {
        return DialectFeatureSet.builder()
                .support(DialectFeature.DELETE_RETURNING, DialectFeatureSupport.SUPPORTED)
                .support(DialectFeature.UPSERT, DialectFeatureSupport.ALTERNATIVE_SYNTAX)
                .support(DialectFeature.SELECT_FOR_UPDATE, DialectFeatureSupport.SUPPORTED)
                .support(DialectFeature.JDBC_BATCH, DialectFeatureSupport.JDBC_DRIVER)
                .support(DialectFeature.JDBC_GENERATED_KEYS, DialectFeatureSupport.JDBC_DRIVER)
                .support(DialectFeature.POSITIONAL_PARAMETERS, DialectFeatureSupport.JDBC_DRIVER)
                .build();
    }
}
