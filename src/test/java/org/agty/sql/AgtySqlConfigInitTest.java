package org.agty.sql;

import org.agty.sql.config.AgtySqlConfig;
import org.agty.sql.config.AgtySqlConfigInit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgtySqlConfigInitTest {

    @Test
    void loadsMysqlSectionFromDefaultConfig() {
        AgtySqlConfig config = new AgtySqlConfigInit("mysql").getConfig();

        assertEquals("mysql", config.getDriver());
        assertEquals("localhost", config.getServer());
        assertEquals(23307, config.getPort());
        assertEquals("agty_sql", config.getUser());
        assertEquals("agty_sql", config.getDatabase());
    }

    @Test
    void loadsMariadbSectionFromDefaultConfig() {
        AgtySqlConfig config = new AgtySqlConfigInit("mariadb").getConfig();

        assertEquals("mariadb", config.getDriver());
        assertEquals("localhost", config.getServer());
        assertEquals(23316, config.getPort());
        assertEquals("agty_sql", config.getUser());
        assertEquals("agty_sql", config.getDatabase());
    }

    @Test
    void loadsPgsqlSectionFromDefaultConfig() {
        AgtySqlConfig config = new AgtySqlConfigInit("pgsql").getConfig();

        assertEquals("pgsql", config.getDriver());
        assertEquals("localhost", config.getServer());
        assertEquals(25432, config.getPort());
        assertEquals("agty_sql", config.getUser());
        assertEquals("public", config.getSchema());
    }

    @Test
    void loadsFileDatabaseSectionsFromDefaultConfig() {
        AgtySqlConfig sqlite = new AgtySqlConfigInit("sqlite").getConfig();
        AgtySqlConfig h2 = new AgtySqlConfigInit("h2").getConfig();

        assertEquals("sqlite", sqlite.getDriver());
        assertEquals("databases/sqlite/agty_sql.sqlite", sqlite.getDatabase());
        assertEquals(0, sqlite.getPort());

        assertEquals("h2", h2.getDriver());
        assertEquals("databases/h2/agty_sql", h2.getDatabase());
        assertEquals("PUBLIC", h2.getSchema());
    }
}
