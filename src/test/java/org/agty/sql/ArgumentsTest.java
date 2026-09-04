package org.agty.sql;

import org.agty.sql.data.Arguments;
import org.agty.sql.data.SqlExpression;
import org.agty.sql.model.ModelAttributes;
import org.agty.sql.model.SaveModelMode;
import org.agty.sql.model.annotations.Entity;
import org.agty.sql.model.annotations.Id;
import org.agty.sql.model.annotations.Table;
import org.agty.sql.model.builders.ModelArgumentsBuilder;
import org.agty.sql.model.entity.ColumnEntity;
import org.agty.sql.support.SqlTextUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentsTest {

    @Test
    void formatsWhereClause() {
        Arguments arguments = Arguments.builder()
                .setWhere("[table] = '%s' AND id = %d", "my_table", 1);

        assertEquals("[table] = 'my_table' AND id = 1", arguments.getWhere());
    }

    @Test
    void preservesPreparedWhereTemplateAndParameters() {
        Arguments arguments = Arguments.builder()
                .setWhere("[name] = ? AND [id] = ?", "O'Reilly", 7)
                .useStatementPrepare(true);

        assertTrue(arguments.useStatementPrepare());
        assertEquals("[name] = ? AND [id] = ?", arguments.getWhere());
        assertEquals(List.of("O'Reilly", 7), arguments.getWhereParameters());
    }

    @Test
    void keepsLegacyWhereFormattingWhenPreparedModeIsDisabled() {
        Arguments arguments = Arguments.builder()
                .setWhere("[name] = '%s' AND [id] = %d", "legacy", 7);

        assertFalse(arguments.useStatementPrepare());
        assertEquals("[name] = 'legacy' AND [id] = 7", arguments.getWhere());
    }

    @Test
    void encodesLegacyWhereStringsWithoutChangingUnicode() {
        String value = "'\"&<>\\\n\r\t Привет 世界";

        Arguments arguments = Arguments.builder().setWhere("[value] = '%s'", value);

        assertEquals(
                "[value] = '&apos;&quot;&amp;&lt;&gt;&#92;&#10;&#13;&#9; "
                        + "&#1055;&#1088;&#1080;&#1074;&#1077;&#1090; &#19990;&#30028;'",
                arguments.getWhere()
        );
        assertEquals(value, SqlTextUtils.hdecode(
                "&apos;&quot;&amp;&lt;&gt;&#92;&#10;&#13;&#9; Привет 世界"
        ));
    }

    @Test
    void neutralizesLegacyWhereInjectionPayload() {
        Arguments arguments = Arguments.builder()
                .setWhere("[name] = '%s'", "' OR 1=1 --");

        assertEquals("[name] = '&apos; OR 1=1 --'", arguments.getWhere());
    }

    @Test
    void rejectsUnsafeLegacyWhereFormatting() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Arguments.builder().setWhere("[name] = %s", "unquoted")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> Arguments.builder().setWhere("[id] = %d", "1 OR 1=1")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> Arguments.builder().setWhere("[value] = '%s'", (Object) null)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> Arguments.builder().setWhere("[value] = '%s'", new Object())
        );
    }

    @Test
    void keepsLiteralHtmlEntitiesDistinctWhenDecoded() {
        String value = "&apos; &#92; &amp;";

        assertEquals(value, SqlTextUtils.hdecode(SqlTextUtils.hencode(value)));
    }

    @Test
    void acceptsOnlyExplicitTrustForExpressionOverloads() {
        Arguments arguments = Arguments.builder()
                .setWhere(SqlExpression.trusted("[deleted_at] IS NULL"))
                .appendWhere(SqlExpression.trusted(" AND [active] = TRUE"))
                .setQuery(SqlExpression.trusted("SELECT 1"));

        assertEquals("[deleted_at] IS NULL AND [active] = TRUE", arguments.getWhere());
        assertEquals("SELECT 1", arguments.getQuery());
    }

    @Test
    @SuppressWarnings("deprecation")
    void rejectsNonEmptyRawStringClauses() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Arguments.builder().setWhere("[id] = 1")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> Arguments.builder().appendWhere(" AND [id] = 1")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> Arguments.builder().setQuery("SELECT 1")
        );
    }

    @Test
    void storesPreparedRawQueryParameters() {
        Arguments arguments = Arguments.builder()
                .setQuery("SELECT * FROM {users} WHERE [id] = ?", 9)
                .useStatementPrepare(true);

        assertEquals("SELECT * FROM {users} WHERE [id] = ?", arguments.getQuery());
        assertEquals(List.of(9), arguments.getQueryParameters());
    }

    @Test
    void appendsPreparedWhereParametersInDeclarationOrder() {
        Arguments arguments = Arguments.builder()
                .setWhere("[id] >= ?", 10)
                .appendWhere(" AND [name] = ?", "name")
                .useStatementPrepare(true);

        assertEquals("[id] >= ? AND [name] = ?", arguments.getWhere());
        assertEquals(List.of(10, "name"), arguments.getWhereParameters());
    }

    @Test
    void doesNotApplyStringFormattingToPreparedSqlOperators() {
        Arguments arguments = Arguments.builder()
                .useStatementPrepare(true)
                .setWhere("[id] % ? = ?", 2, 0);

        assertEquals("[id] % ? = ?", arguments.getWhere());
        assertEquals(List.of(2, 0), arguments.getWhereParameters());
    }

    @Test
    void preservesOriginalDataForPreparedModeAndLegacyDataByDefault() {
        Arguments arguments = Arguments.builder().addData("path", "C:\\temp\\");

        assertEquals("C:\\temp\\", arguments.getData("path"));

        arguments.useStatementPrepare(true);

        assertEquals("C:\\temp\\", arguments.getData("path"));
        assertEquals(List.of("C:\\temp\\"), arguments.getDataValues());
    }

    @Test
    void storesDataAndColumns() {
        Arguments arguments = Arguments.builder()
                .setTable("{users}")
                .setActionField("id")
                .addData("name", "alice")
                .addData("age", 42)
                .addColumn("id")
                .addColumn("name");

        assertEquals("{users}", arguments.getTable());
        assertEquals("id", arguments.getActionField());
        assertEquals("alice", arguments.getData("name"));
        assertEquals(42, arguments.getData("age"));
        assertEquals(2, arguments.dataSize());
        assertEquals(2, arguments.getColumns().size());
        assertTrue(arguments.hasData());
        assertTrue(arguments.hasColumns());
        assertTrue(arguments.hasActionField());
    }

    @Test
    void storesValuesThroughExplicitRuntimeTypeChecks() {
        Object name = "Alex";
        Object age = 30;
        Object active = true;

        Arguments arguments = Arguments.builder()
                .addDataString("name", name)
                .addDataInt("age", age)
                .addDataBoolean("active", active)
                .addDataLong("visits", 15L)
                .addDataShort("rank", (short) 2)
                .addDataByte("flags", (byte) 1)
                .addDataFloat("ratio", 1.25F)
                .addDataDouble("score", 2.5D)
                .addDataChar("code", 'A')
                .addDataNull("deleted_at");

        assertEquals("Alex", arguments.getData("name"));
        assertEquals(30, arguments.getData("age"));
        assertEquals(true, arguments.getData("active"));
        assertEquals((byte) 1, arguments.getData("flags"));
        assertNull(arguments.getData("deleted_at"));
    }

    @Test
    void acceptsAnyNumericValueThroughDecimalMethod() {
        Arguments arguments = Arguments.builder()
                .addDataDecimal("integer", 10)
                .addDataDecimal("decimal", new BigDecimal("12.340"))
                .addDataDecimal("large", new BigInteger("9223372036854775808"))
                .addDataDecimal("atomic", new AtomicInteger(7));

        assertEquals(10, arguments.getData("integer"));
        assertEquals(new BigDecimal("12.340"), arguments.getData("decimal"));
        assertEquals(new BigInteger("9223372036854775808"), arguments.getData("large"));
        assertEquals(new BigDecimal("7"), arguments.getData("atomic"));
    }

    @Test
    void rejectsRuntimeTypeMismatchInNamedMethod() {
        Object value = 30L;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Arguments.builder().addDataInt("age", value)
        );

        assertTrue(exception.getMessage().contains("expected Integer"));
        assertTrue(exception.getMessage().contains("java.lang.Long"));
    }

    @Test
    void rejectsUnsupportedDynamicDataType() {
        Object value = Map.of("unsafe", "object");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Arguments.builder().addData("payload", value)
        );

        assertTrue(exception.getMessage().contains("Unsupported data type"));
        assertTrue(exception.getMessage().contains("java.util"));
    }

    @Test
    void rejectsNullDataFieldName() {
        IllegalArgumentException valueException = assertThrows(
                IllegalArgumentException.class,
                () -> Arguments.builder().addDataString(null, "Alex")
        );
        IllegalArgumentException nullValueException = assertThrows(
                IllegalArgumentException.class,
                () -> Arguments.builder().addDataNull(null)
        );

        assertEquals("SQL data field must not be null or blank", valueException.getMessage());
        assertEquals("SQL data field must not be null or blank", nullValueException.getMessage());
    }

    @Test
    void acceptsValidatedStructuralIdentifiers() {
        Arguments arguments = Arguments.builder()
                .setTable("{users}")
                .setActionField("id")
                .setPrimaryKey("id")
                .addColumn("name")
                .addDataString("name", "Alex")
                .setFields("users.id, [name], *")
                .setGroupBy("users.id, [name]")
                .setOrderBy("users.id DESC, [name] ASC NULLS LAST");

        assertEquals("{users}", arguments.getTable());
        assertEquals("users.id, [name], *", arguments.getFields());
        assertEquals("users.id, [name]", arguments.getGroupBy());
        assertEquals("users.id DESC, [name] ASC NULLS LAST", arguments.getOrderBy());
    }

    @SuppressWarnings("deprecation")
    @Test
    void rejectsUntrustedStructuralSql() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> Arguments.builder().setTable("{users}; DROP TABLE users")
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> Arguments.builder().addDataString("name\"='admin' --", "Alex")
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> Arguments.builder().setActionField("id DESC")
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> Arguments.builder().addColumn("id, password")
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> Arguments.builder().setPrimaryKey("id--")
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> Arguments.builder().setFields("id, COUNT(*)")
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> Arguments.builder().setGroupBy("id; DELETE FROM users")
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> Arguments.builder().setOrderBy("id DESC; DELETE FROM users")
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> Arguments.builder().setHaving("COUNT(*) > 1")
                )
        );
    }

    @SuppressWarnings("deprecation")
    @Test
    void validatesDeprecatedDirectTableAssignmentOnRead() {
        Arguments arguments = Arguments.builder();
        arguments.table = "{users}; DROP TABLE users";

        assertThrows(IllegalArgumentException.class, arguments::getTable);
    }

    @Test
    void acceptsExplicitlyTrustedStructuralExpressions() {
        Arguments arguments = Arguments.builder()
                .setFields(SqlExpression.trusted("user_id, COUNT(*) AS total"))
                .setGroupBy(SqlExpression.trusted("date_trunc('day', created_at)"))
                .setHaving(SqlExpression.trusted("COUNT(*) > 3"))
                .setOrderBy(SqlExpression.trusted("COUNT(*) DESC"));

        assertEquals("user_id, COUNT(*) AS total", arguments.getFields());
        assertEquals("date_trunc('day', created_at)", arguments.getGroupBy());
        assertEquals("COUNT(*) > 3", arguments.getHaving());
        assertEquals("COUNT(*) DESC", arguments.getOrderBy());
        assertThrows(IllegalArgumentException.class, () -> SqlExpression.trusted(" "));
    }

    @Test
    void buildsModelSaveArgumentsInPreparedMode() {
        ModelAttributes<?> model = new ModelAttributes<>(new PreparedModelEntity()).build();

        Arguments arguments = ModelArgumentsBuilder.builder()
                .model(model)
                .saveModelMode(SaveModelMode.WITH_CHECK)
                .idColumn(new ColumnEntity())
                .build();

        assertTrue(arguments.useStatementPrepare());
        assertEquals("users", arguments.getTable());
        assertEquals("[id] = ?", arguments.getWhere());
        assertEquals(List.of(7L), arguments.getWhereParameters());
        assertEquals("Alex", arguments.getData("name"));
    }

    @Test
    void acceptsSupportedDynamicDataType() {
        Object value = new BigDecimal("99.50");

        Arguments arguments = Arguments.builder().addData("amount", value);

        assertEquals(new BigDecimal("99.50"), arguments.getData("amount"));
    }

    @Test
    void rejectsNonFiniteDecimalValues() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Arguments.builder().addDataDecimal("amount", Double.NaN)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> Arguments.builder().addDataFloat("amount", Float.POSITIVE_INFINITY)
        );
    }

    @Test
    void keepsFlagsExplicit() {
        Arguments arguments = Arguments.builder()
                .setNoStringEncode(true)
                .setNoRebuildQuery(true)
                .setForceRebuildQuery(true)
                .setReturnLastInsertId(true);

        assertTrue(arguments.noStringEncode());
        assertTrue(arguments.noRebuildQuery());
        assertTrue(arguments.forceRebuildQuery());
        assertTrue(arguments.returnLastInsertId());
    }

    @Test
    void convertsBooleanValuesByDriver() {
        Arguments arguments = Arguments.builder();

        assertEquals(1, arguments.getBooleanValueForDriver(true, "mysql"));
        assertEquals(0, arguments.getBooleanValueForDriver(false, "mariadb"));
        assertEquals(1, arguments.getBooleanValueForDriver(true, "mssql"));
        assertEquals(0, arguments.getBooleanValueForDriver(false, "sqlite"));
        assertEquals(1, arguments.getBooleanValueForDriver(true, "h2"));
        assertEquals(true, arguments.getBooleanValueForDriver(true, "pgsql"));
        assertEquals(false, arguments.getBooleanValueForDriver(false, "postgresql"));
    }

    @Test
    void storesDriverAwareBooleanData() {
        Arguments arguments = Arguments.builder()
                .addData("mysql_flag", true, "mysql")
                .addData("pgsql_flag", false, "pgsql");

        assertEquals(1, arguments.getData("mysql_flag"));
        assertEquals(false, arguments.getData("pgsql_flag"));
    }

    @Test
    void rejectsUnknownDriverForBooleanConversion() {
        Arguments arguments = Arguments.builder();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> arguments.getBooleanValueForDriver(true, "oracle")
        );

        assertTrue(exception.getMessage().contains("Unsupported driver"));
    }

    @Entity
    @Table(name = "users")
    static class PreparedModelEntity {
        @Id
        Long id = 7L;

        String name = "Alex";
    }
}
