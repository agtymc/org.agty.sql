package org.agty.sql;

import org.agty.sql.support.Logger;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Bounded process-wide writer that keeps query-log file I/O out of JDBC calls.
 */
final class AsyncQueryLogWriter implements AutoCloseable {
    private static final int DEFAULT_CAPACITY = 4096;
    private static final Duration SHUTDOWN_TIMEOUT = Duration.ofSeconds(2);
    private static final AsyncQueryLogWriter SHARED = new AsyncQueryLogWriter(DEFAULT_CAPACITY);

    private final BlockingQueue<LogEntry> queue;
    private final EntryWriter entryWriter;
    private final AtomicBoolean accepting = new AtomicBoolean(true);
    private final AtomicInteger inFlight = new AtomicInteger();
    private final AtomicReference<IOException> failure = new AtomicReference<>();
    private final Thread worker;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> SHARED.close(SHUTDOWN_TIMEOUT),
                "agty-sql-query-log-shutdown"
        ));
    }

    private AsyncQueryLogWriter(int capacity) {
        this(capacity, entry -> new Logger(entry.fileName()).append(entry.message()));
    }

    AsyncQueryLogWriter(int capacity, EntryWriter entryWriter) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Query log queue capacity must be positive");
        }
        this.queue = new ArrayBlockingQueue<>(capacity);
        this.entryWriter = Objects.requireNonNull(entryWriter, "entryWriter");
        this.worker = Thread.ofPlatform()
                .daemon(true)
                .name("agty-sql-query-log")
                .start(this::runWorker);
    }

    static AsyncQueryLogWriter shared() {
        return SHARED;
    }

    boolean append(String fileName, String message) {
        Objects.requireNonNull(fileName, "fileName");
        Objects.requireNonNull(message, "message");
        return accepting.get() && queue.offer(new LogEntry(fileName, message));
    }

    IOException pollFailure() {
        return failure.getAndSet(null);
    }

    boolean flush(Duration timeout) {
        Objects.requireNonNull(timeout, "timeout");
        if (timeout.isNegative()) {
            throw new IllegalArgumentException("Flush timeout must not be negative");
        }

        long deadline = System.nanoTime() + timeout.toNanos();
        while ((!queue.isEmpty() || inFlight.get() != 0) && System.nanoTime() < deadline) {
            try {
                TimeUnit.MILLISECONDS.sleep(1L);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return queue.isEmpty() && inFlight.get() == 0;
    }

    @Override
    public void close() {
        close(SHUTDOWN_TIMEOUT);
    }

    private void close(Duration timeout) {
        if (!accepting.compareAndSet(true, false)) {
            return;
        }
        flush(timeout);
        worker.interrupt();
        try {
            worker.join(Math.max(1L, timeout.toMillis()));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private void runWorker() {
        while (accepting.get() || !queue.isEmpty()) {
            try {
                LogEntry entry = queue.poll(100L, TimeUnit.MILLISECONDS);
                if (entry == null) {
                    continue;
                }
                inFlight.incrementAndGet();
                try {
                    entryWriter.write(entry);
                } catch (IOException exception) {
                    failure.compareAndSet(null, exception);
                } catch (RuntimeException exception) {
                    failure.compareAndSet(
                            null,
                            new IOException("Query log writer failed", exception)
                    );
                } finally {
                    inFlight.decrementAndGet();
                }
            } catch (InterruptedException exception) {
                if (accepting.get()) {
                    continue;
                }
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    record LogEntry(String fileName, String message) {
    }

    @FunctionalInterface
    interface EntryWriter {
        void write(LogEntry entry) throws IOException;
    }
}
