package org.agty.sql.compatibility;

import org.agty.sql.AgtySQL;
import org.agty.sql.config.AgtySqlConfig;
import org.agty.sql.data.Arguments;
import org.agty.sql.interfaces.SqlRow;

import java.util.LinkedList;

/** Client compiled against 2.0.4 and executed unchanged against current 2.x. */
public final class Legacy2xClient {
    private Legacy2xClient() {
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected an H2 database path");
        }

        AgtySqlConfig config = new AgtySqlConfig()
                .setDriver("h2")
                .setDatabase(args[0])
                .setSchema("PUBLIC")
                .setPfx("")
                .setThrowException(true)
                .setDebug(false);

        AgtySQL sql = new AgtySQL(config);
        sql.execute("DROP TABLE IF EXISTS {legacy_users}");
        sql.execute("CREATE TABLE {legacy_users} (id BIGINT PRIMARY KEY, name VARCHAR(255))");

        Arguments insert = Arguments.builder()
                .setTable("{legacy_users}")
                .setData("id", 1L)
                .setData("name", "legacy-client");
        sql.insert(insert);

        Arguments allRows = Arguments.builder().setTable("{legacy_users}");
        require(sql.rows(allRows) == 1L, "rows alias returned an unexpected count");

        SqlRow row = sql.getByField("{legacy_users}", "id", "1");
        require(row != null, "getByField returned null");
        require("legacy-client".equals(row.getString("name")), "legacy row value changed");
        require(!row.setDataIsString(false).dataIsString(), "legacy SqlRow aliases changed");

        LinkedList<SqlRow> rows = sql.findAll(allRows);
        require(rows.size() == 1, "findAll alias returned an unexpected row count");

        Arguments delete = Arguments.builder()
                .setTable("{legacy_users}")
                .setWhere("[id] = %d", 1);
        require(sql.del(delete), "del alias failed");
        require(sql.rows(allRows) == 0L, "legacy delete did not remove the row");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
