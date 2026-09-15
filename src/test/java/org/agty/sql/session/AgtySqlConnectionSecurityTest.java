package org.agty.sql.session;

import org.agty.sql.config.AgtySqlConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgtySqlConnectionSecurityTest {

    @Test
    void sqlServerValidatesCertificatesByDefault() {
        AgtySqlConfig config = sqlServerConfig();

        String uri = new AgtySqlConnection(config, "sqlserver").getConnectionURI();

        assertTrue(uri.contains("encrypt=true;"));
        assertTrue(uri.contains("trustServerCertificate=false;"));
    }

    @Test
    void sqlServerTrustOverrideMustBeExplicitAndSurvivesConfigClone() {
        AgtySqlConfig config = AgtySqlConfig.getClone(
                sqlServerConfig().setTrustServerCertificate(true)
        );

        assertTrue(config.isTrustServerCertificate());
        assertTrue(new AgtySqlConnection(config, "sqlserver")
                .getConnectionURI()
                .contains("trustServerCertificate=true;"));
        assertFalse(sqlServerConfig().isTrustServerCertificate());
    }

    @Test
    void connectionAndSocketTimeoutsAreSeparateDriverProperties() {
        AgtySqlConfig mysql = new AgtySqlConfig()
                .setDriver("mysql")
                .setServer("db.example.test")
                .setPort(3306)
                .setDatabase("app")
                .setLoginTimeoutSeconds(12)
                .setNetworkTimeoutMillis(34_000);
        String mysqlUri = new AgtySqlConnection(mysql, "mysql").getConnectionURI();

        assertTrue(mysqlUri.contains("connectTimeout=12000"));
        assertTrue(mysqlUri.contains("socketTimeout=34000"));

        AgtySqlConfig sqlServer = sqlServerConfig()
                .setLoginTimeoutSeconds(12)
                .setNetworkTimeoutMillis(34_000);
        String sqlServerUri = new AgtySqlConnection(sqlServer, "sqlserver").getConnectionURI();

        assertTrue(sqlServerUri.contains("loginTimeout=12;"));
        assertTrue(sqlServerUri.contains("socketTimeout=34000;"));
    }

    @Test
    void clickHouseUsesDedicatedJdbcUrlProperties() {
        AgtySqlConfig clickHouse = new AgtySqlConfig()
                .setDriver("clickhouse")
                .setServer("db.example.test")
                .setPort(8123)
                .setDatabase("events")
                .setLoginTimeoutSeconds(12)
                .setNetworkTimeoutMillis(34_000);

        String uri = new AgtySqlConnection(clickHouse, "clickhouse").getConnectionURI();

        assertTrue(uri.startsWith("jdbc:clickhouse://db.example.test:8123/events?"));
        assertTrue(uri.contains("connection_timeout=12000"));
        assertTrue(uri.contains("socket_timeout=34000"));
        assertFalse(uri.contains("serverTimezone"));
        assertFalse(uri.contains("characterEncoding"));
    }

    private AgtySqlConfig sqlServerConfig() {
        return new AgtySqlConfig()
                .setDriver("mssql")
                .setServer("db.example.test")
                .setPort(1433)
                .setDatabase("app")
                .setUser("app")
                .setPassword("secret");
    }
}
