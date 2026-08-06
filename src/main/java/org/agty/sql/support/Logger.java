package org.agty.sql.support;

import org.agty.sql.exceptions.LoggerException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Internal file logger used by current production code.
 */
public final class Logger {
    private String filename = "sql.log";

    public Logger(String fileName) {
        this.filename = fileName;
    }

    public void write(String string) throws IOException {
        fileOperation(string, StandardOpenOption.WRITE);
    }

    public void append(String string, String nl) throws IOException {
        fileOperation(string + nl, StandardOpenOption.APPEND);
    }

    public void append(String string) throws IOException {
        append(string, "\n");
    }

    private void fileOperation(String string, StandardOpenOption standardOpenOption) throws IOException {
        Files.write(Paths.get(filename), string.getBytes(), standardOpenOption);
    }

    public void clear() throws IOException {
        write("");
    }

    public void remove() throws LoggerException {
        try {
            File file = new File(filename);
            if (!file.isDirectory()) {
                Files.delete(file.getAbsoluteFile().toPath());
            }
        } catch (NoSuchFileException e) {
            throw new LoggerException("File not found: " + filename);
        } catch (IOException e) {
            throw new LoggerException(e.getMessage());
        }
    }
}
