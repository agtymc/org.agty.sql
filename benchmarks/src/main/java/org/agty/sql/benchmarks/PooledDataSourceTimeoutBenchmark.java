package org.agty.sql.benchmarks;

import org.agty.sql.config.AgtySqlConfig;
import org.agty.sql.datasource.AgtySqlPooledDataSource;
import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
@Fork(2)
@Threads(8)
public class PooledDataSourceTimeoutBenchmark {
    private static final long HOLD_NANOS = TimeUnit.MILLISECONDS.toNanos(100L);

    private Path databaseDirectory;
    private Path databasePath;
    private AgtySqlPooledDataSource dataSource;
    private Connection reservedConnection;

    @Setup(Level.Trial)
    public void setUp() throws Exception {
        databaseDirectory = Files.createTempDirectory("agty-sql-timeout-jmh-");
        databasePath = databaseDirectory.resolve("pool-timeout");
        AgtySqlConfig config = new AgtySqlConfig()
                .setDriver("h2")
                .setDatabase(databasePath.toString())
                .setSchema("PUBLIC")
                .setPfx("")
                .setThrowException(true)
                .setDebug(false);

        dataSource = new AgtySqlPooledDataSource(
                config,
                2,
                2,
                Duration.ofMillis(250L),
                Duration.ofMinutes(5),
                Duration.ofMinutes(5)
        );
        reservedConnection = dataSource.getConnection();
    }

    @Benchmark
    public void borrowUnderSaturation(TimeoutCounters counters) throws SQLException {
        try (Connection ignored = dataSource.getConnection()) {
            counters.successfulBorrows++;
            LockSupport.parkNanos(HOLD_NANOS);
        } catch (SQLTransientConnectionException exception) {
            counters.timeouts++;
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() throws Exception {
        if (reservedConnection != null) {
            reservedConnection.close();
        }
        if (dataSource != null) {
            dataSource.close();
        }
        if (databasePath != null) {
            Files.deleteIfExists(Path.of(databasePath + ".mv.db"));
            Files.deleteIfExists(Path.of(databasePath + ".trace.db"));
        }
        if (databaseDirectory != null) {
            Files.deleteIfExists(databaseDirectory);
        }
    }

    @AuxCounters(AuxCounters.Type.EVENTS)
    @State(Scope.Thread)
    public static class TimeoutCounters {
        public long successfulBorrows;
        public long timeouts;
    }
}
