package org.agty.sql.support;

import org.agty.sql.exceptions.LoggerException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

/**
 * Internal file logger used by current production code.
 */
public final class Logger {
    private static final long DEFAULT_MAX_FILE_SIZE = 10L * 1024L * 1024L;
    private static final Object FILE_LOCK = new Object();
    private static final Set<PosixFilePermission> OWNER_ONLY = Set.of(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE
    );

    private final Path file;
    private final long maxFileSize;

    public Logger(String fileName) {
        this(fileName, DEFAULT_MAX_FILE_SIZE);
    }

    public Logger(String fileName, long maxFileSize) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Log filename must not be null or blank");
        }
        if (maxFileSize < 1) {
            throw new IllegalArgumentException("Maximum log file size must be positive");
        }
        this.file = Path.of(fileName);
        this.maxFileSize = maxFileSize;
    }

    public void write(String string) throws IOException {
        synchronized (FILE_LOCK) {
            prepareFile();
            Files.writeString(
                    file,
                    string,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
            restrictPermissions();
        }
    }

    public void append(String string, String nl) throws IOException {
        byte[] data = (string + nl).getBytes(StandardCharsets.UTF_8);
        synchronized (FILE_LOCK) {
            prepareFile();
            rotateIfRequired(data.length);
            Files.write(file, data, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            restrictPermissions();
        }
    }

    public void append(String string) throws IOException {
        append(string, "\n");
    }

    private void prepareFile() throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
    }

    private void rotateIfRequired(int incomingBytes) throws IOException {
        if (!Files.exists(file) || Files.size(file) + incomingBytes <= maxFileSize) return;
        Path rotated = Path.of(file + ".1");
        Files.move(file, rotated, StandardCopyOption.REPLACE_EXISTING);
        restrictPermissions(rotated);
    }

    private void restrictPermissions() throws IOException {
        restrictPermissions(file);
    }

    private void restrictPermissions(Path target) throws IOException {
        if (Files.getFileStore(target).supportsFileAttributeView("posix")) {
            Files.setPosixFilePermissions(target, OWNER_ONLY);
        }
    }

    public void clear() throws IOException {
        write("");
    }

    public void remove() throws LoggerException {
        try {
            if (!Files.isDirectory(file)) {
                Files.delete(file.toAbsolutePath());
            }
        } catch (NoSuchFileException e) {
            throw new LoggerException("File not found: " + file, e);
        } catch (IOException e) {
            throw new LoggerException(e.getMessage(), e);
        }
    }
}
