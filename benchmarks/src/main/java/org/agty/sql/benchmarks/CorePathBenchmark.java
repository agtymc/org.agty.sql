package org.agty.sql.benchmarks;

import org.agty.sql.base.RowData;
import org.agty.sql.data.Arguments;
import org.agty.sql.data.SqlQueryRebuild;
import org.agty.sql.dialect.pgsql.PgSQL;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
@Fork(2)
public class CorePathBenchmark {
    private static final String QUERY = "SELECT [id], [name], $$ {literal} [literal] $$ "
            + "FROM {users} WHERE [name] = 'O''Reilly' AND [active] = 1 "
            + "/* keep {comment} [comment] */";

    private final PgSQL pgsql = new PgSQL(null);
    private final RowData row = createRow();

    @Benchmark
    public String renderLegacyUpdate() {
        Arguments arguments = Arguments.builder()
                .setTable("{users}")
                .addDataString("name", "O'Reilly & <admin>")
                .addDataDecimal("balance", new BigDecimal("123456.78"))
                .addDataBoolean("active", true)
                .setWhere("[id] = %d", 42);
        return pgsql.updateQuery(arguments);
    }

    @Benchmark
    public String renderPreparedUpdate() {
        Arguments arguments = Arguments.builder()
                .useStatementPrepare(true)
                .setTable("{users}")
                .addDataString("name", "O'Reilly & <admin>")
                .addDataDecimal("balance", new BigDecimal("123456.78"))
                .addDataBoolean("active", true)
                .setWhere("[id] = ?", 42);
        return pgsql.updateQuery(arguments);
    }

    @Benchmark
    public String rebuildStructuredQuery() {
        return new SqlQueryRebuild(QUERY)
                .setPrefix("app_")
                .setQuoteTable("`")
                .setQuoteColumn("`")
                .rebuildAndGet();
    }

    @Benchmark
    public void readConvertedRow(Blackhole blackhole) {
        blackhole.consume(row.getLong("ID"));
        blackhole.consume(row.getDouble("balance"));
        blackhole.consume(row.getLocalDate("created_at"));
        blackhole.consume(row.getBoolean("ACTIVE"));
        blackhole.consume(row.getString("name"));
    }

    private static RowData createRow() {
        RowData data = new RowData();
        data.setData("id", 42L);
        data.setData("balance", new BigDecimal("123456.78"));
        data.setData("created_at", LocalDateTime.of(2026, 9, 4, 12, 30, 45));
        data.setData("active", true);
        data.setData("name", "Alex");
        return data;
    }
}
