package org.agty.sql.benchmarks;

import org.agty.sql.config.AgtySqlConfig;
import org.agty.sql.datasource.AgtySqlPooledDataSource;
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
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
@Fork(2)
@Threads(8)
public class PooledDataSourceBenchmark {
    private Path databaseDirectory;
    private Path databasePath;
    private AgtySqlPooledDataSource dataSource;

    @Setup(Level.Trial)
    public void setUp() throws Exception {
        databaseDirectory = Files.createTempDirectory("agty-sql-jmh-");
        databasePath = databaseDirectory.resolve("pool");
        AgtySqlConfig config = new AgtySqlConfig()
                .setDriver("h2")
                .setDatabase(databasePath.toString())
                .setSchema("PUBLIC")
                .setPfx("")
                .setThrowException(true)
                .setDebug(false);

        dataSource = new AgtySqlPooledDataSource(
                config,
                16,
                8,
                Duration.ofSeconds(5),
                Duration.ofMinutes(5),
                Duration.ofMinutes(5)
        );
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE benchmark_data (id BIGINT PRIMARY KEY, payload VARCHAR(64))");
            statement.execute("INSERT INTO benchmark_data (id, payload) VALUES (1, 'ready')");
        }
    }

    @Benchmark
    public String borrowSelectAndReturn() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT payload FROM benchmark_data WHERE id = 1"
             )) {
            if (!resultSet.next()) {
                throw new IllegalStateException("Benchmark row is missing");
            }
            return resultSet.getString(1);
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() throws Exception {
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
}
