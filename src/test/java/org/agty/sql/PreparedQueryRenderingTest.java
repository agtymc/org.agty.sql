package org.agty.sql;

import org.agty.sql.data.Arguments;
import org.agty.sql.dialect.mssql.MsSQL;
import org.agty.sql.dialect.mysql.MySQL;
import org.agty.sql.dialect.pgsql.PgSQL;
import org.agty.sql.sqlbuilder.SqlValueRenderer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PreparedQueryRenderingTest {

    @Test
    void rendersPostgreSqlUpdateWithPlaceholders() {
        Arguments arguments = preparedUpdateArguments();

        assertEquals(
                "UPDATE {users} SET \"name\"=?,\"description\"=? WHERE [id] = ?",
                new PgSQL(null).updateQuery(arguments)
        );
    }

    @Test
    void rendersMySqlUpdateWithPlaceholders() {
        Arguments arguments = preparedUpdateArguments();

        assertEquals(
                "UPDATE {users} SET `name`=?,`description`=? WHERE [id] = ?",
                new MySQL(null).updateQuery(arguments)
        );
    }

    @Test
    void keepsLegacyHtmlEncodingByDefault() {
        Arguments arguments = Arguments.builder()
                .setTable("{users}")
                .addData("name", "O'Reilly & <admin>")
                .setWhere("[id] = %d", 1);

        assertEquals(
                "UPDATE {users} SET \"name\"='O&apos;Reilly &amp; &lt;admin&gt;' WHERE [id] = 1",
                new PgSQL(null).updateQuery(arguments)
        );
    }

    @Test
    void rendersAllDecimalTypesAsNumericSqlValues() {
        Arguments arguments = Arguments.builder()
                .setTable("{metrics}")
                .addDataDecimal("amount", new BigDecimal("123.45"))
                .addDataDecimal("large", new BigInteger("9223372036854775808"))
                .addDataByte("small", (byte) 7);

        assertEquals(
                "UPDATE {metrics} SET \"amount\"=123.45,\"large\"=9223372036854775808,\"small\"=7",
                new PgSQL(null).updateQuery(arguments)
        );
        assertEquals(
                "UPDATE {metrics} SET `amount`=123.45,`large`=9223372036854775808,`small`=7",
                new MySQL(null).updateQuery(arguments)
        );
        assertEquals(
                "UPDATE {metrics} SET \"amount\"=123.45,\"large\"=9223372036854775808,\"small\"=7",
                new MsSQL(null).updateQuery(arguments)
        );
    }

    @Test
    void rendersValuesThroughRenamedRenderer() {
        assertEquals(
                "\"name\"='O&apos;Reilly'",
                new SqlValueRenderer()
                        .setQuoteColumn("\"")
                        .setQuoteValue("'")
                        .setColumn("name")
                        .setValue("O'Reilly")
                        .render()
        );
    }

    private Arguments preparedUpdateArguments() {
        return Arguments.builder()
                .useStatementPrepare(true)
                .setTable("{users}")
                .addData("name", "O'Reilly")
                .addData("description", "")
                .setWhere("[id] = ?", 1);
    }
}
