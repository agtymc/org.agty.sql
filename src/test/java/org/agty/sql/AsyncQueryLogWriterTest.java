package org.agty.sql;

import org.agty.sql.config.AgtySqlConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class AsyncQueryLogWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void preservesOrderAndFlushesAcceptedEntries() {
        List<String> messages = new CopyOnWriteArrayList<>();
        try (AsyncQueryLogWriter writer = new AsyncQueryLogWriter(
                4,
                entry -> messages.add(entry.message())
        )) {
            Assertions.assertTrue(writer.append("query.log", "first"));
            Assertions.assertTrue(writer.append("query.log", "second"));
            Assertions.assertTrue(writer.flush(Duration.ofSeconds(1)));
        }

        Assertions.assertEquals(List.of("first", "second"), messages);
    }

    @Test
    void rejectsWithoutBlockingWhenBoundedQueueIsFull() throws InterruptedException {
        CountDownLatch writerStarted = new CountDownLatch(1);
        CountDownLatch releaseWriter = new CountDownLatch(1);
        try (AsyncQueryLogWriter writer = new AsyncQueryLogWriter(1, entry -> {
            writerStarted.countDown();
            try {
                releaseWriter.await();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        })) {
            Assertions.assertTrue(writer.append("query.log", "in-flight"));
            Assertions.assertTrue(writerStarted.await(1, TimeUnit.SECONDS));
            Assertions.assertTrue(writer.append("query.log", "queued"));

            long started = System.nanoTime();
            Assertions.assertFalse(writer.append("query.log", "dropped"));
            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
            Assertions.assertTrue(elapsedMillis < 500L);

            releaseWriter.countDown();
            Assertions.assertTrue(writer.flush(Duration.ofSeconds(1)));
        }
    }

    @Test
    void exposesBackgroundIoFailure() {
        IOException expected = new IOException("disk unavailable");
        try (AsyncQueryLogWriter writer = new AsyncQueryLogWriter(1, entry -> {
            throw expected;
        })) {
            Assertions.assertTrue(writer.append("query.log", "query"));
            Assertions.assertTrue(writer.flush(Duration.ofSeconds(1)));
            Assertions.assertSame(expected, writer.pollFailure());
            Assertions.assertNull(writer.pollFailure());
        }
    }

    @Test
    void containsUncheckedWriterFailure() {
        IllegalArgumentException expected = new IllegalArgumentException("invalid path");
        try (AsyncQueryLogWriter writer = new AsyncQueryLogWriter(1, entry -> {
            throw expected;
        })) {
            Assertions.assertTrue(writer.append("query.log", "query"));
            Assertions.assertTrue(writer.flush(Duration.ofSeconds(1)));

            IOException failure = writer.pollFailure();
            Assertions.assertNotNull(failure);
            Assertions.assertSame(expected, failure.getCause());
        }
    }

    @Test
    void facadeQueuesSanitizedQueryLog() throws IOException {
        Path logFile = tempDir.resolve("query.log");
        AgtySqlConfig config = new AgtySqlConfig()
                .setDriver("h2")
                .setDatabase(tempDir.resolve("async-query-log-db").toString())
                .setSchema("PUBLIC")
                .setPfx("")
                .setLogQuery(true)
                .setLogQueryFile(logFile.toString());

        try (AgtySQL sql = new AgtySQL(config)) {
            Assertions.assertTrue(sql.execute("SELECT 'secret-value'"));
            Assertions.assertTrue(AsyncQueryLogWriter.shared().flush(Duration.ofSeconds(1)));
        }

        String log = Files.readString(logFile, StandardCharsets.UTF_8);
        Assertions.assertFalse(log.contains("secret-value"));
        Assertions.assertTrue(log.contains("SELECT '***'"));
    }
}
