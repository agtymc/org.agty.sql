package org.agty.sql;

import org.agty.sql.config.AgtySqlConfig;
import org.agty.sql.config.AgtySqlConfigFile;
import org.agty.sql.session.AgtySqlConnector;
import org.agty.sql.exceptions.AgtySqlException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class AgtySqlConfigSecurityTest {

    @TempDir
    Path tempDir;

    @Test
    void defaultsFailClosedWithoutCredentials() {
        AgtySqlConfig config = new AgtySqlConfig();

        Assertions.assertNull(config.getUser());
        Assertions.assertNull(config.getPassword());
        Assertions.assertTrue(config.isThrowException());
        Assertions.assertEquals("errors.log", config.getLogErrorsFileOrDefault("errors.log"));
        Assertions.assertEquals("queries.log", config.getLogQueryFileOrDefault("queries.log"));
        Assertions.assertFalse(new AgtySqlConnector(config).isCheckConfig());
    }

    @Test
    void clonePreservesIndependentTimeoutsAndLogPolicy() {
        AgtySqlConfig clone = AgtySqlConfig.getClone(new AgtySqlConfig()
                .setLoginTimeoutSeconds(12)
                .setNetworkTimeoutMillis(34_000)
                .setLogQueryValues(true));

        Assertions.assertEquals(12, clone.getLoginTimeoutSeconds());
        Assertions.assertEquals(34_000, clone.getNetworkTimeoutMillis());
        Assertions.assertTrue(clone.isLogQueryValues());
    }

    @Test
    void fileConfigDoesNotInventRootCredentials() throws IOException {
        Path configFile = tempDir.resolve("database.properties");
        Files.writeString(configFile, """
                db.test.driver=mysql
                db.test.server=localhost
                db.test.port=3306
                db.test.database=test
                """);

        AgtySqlConfig config = new AgtySqlConfigFile("test", configFile.toString()).getConfig();

        Assertions.assertNull(config.getUser());
        Assertions.assertNull(config.getPassword());
        Assertions.assertTrue(config.isThrowException());
        Assertions.assertFalse(new AgtySqlConnector(config).isCheckConfig());
    }

    @Test
    void fileConfigExpandsEnvironmentReferencesBeforeTypedParsing() throws IOException {
        Path configFile = tempDir.resolve("environment.properties");
        Files.writeString(configFile, """
                db.test.driver=h2
                db.test.port=${PATH}
                """);

        Assertions.assertThrows(
                NumberFormatException.class,
                () -> new AgtySqlConfigFile("test", configFile.toString())
        );

        Files.writeString(configFile, """
                db.test.driver=h2
                db.test.database=${PATH}
                db.test.stmtRows=125
                """);
        AgtySqlConfig config = new AgtySqlConfigFile("test", configFile.toString()).getConfig();

        Assertions.assertEquals(System.getenv("PATH"), config.getDatabase());
        Assertions.assertEquals(125, config.getStmtRows());
    }

    @Test
    void infrastructureFailuresThrowTypedExceptionWithOriginalCause() {
        AgtySqlConfig config = new AgtySqlConfig()
                .setDriver("h2")
                .setDatabase(tempDir.resolve("errors").toString())
                .setUser("sa")
                .setPassword("");

        try (AgtySQL sql = new AgtySQL(config)) {
            AgtySqlException exception = Assertions.assertThrows(
                    AgtySqlException.class,
                    () -> sql.execute("SELECT * FROM [missing_table]")
            );
            Assertions.assertInstanceOf(java.sql.SQLException.class, exception.getCause());
        }
    }
}
