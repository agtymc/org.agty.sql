package org.agty.sql.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

class LoggerSecurityTest {

    @TempDir
    Path tempDir;

    @Test
    void createsDirectoriesAndWritesUtf8WithRestrictedPermissions() throws IOException {
        Path log = tempDir.resolve("nested/query.log");

        new Logger(log.toString()).append("Привет");

        Assertions.assertEquals("Привет\n", Files.readString(log, StandardCharsets.UTF_8));
        if (Files.getFileStore(log).supportsFileAttributeView("posix")) {
            Assertions.assertEquals(
                    Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE),
                    Files.getPosixFilePermissions(log)
            );
        }
    }

    @Test
    void rotatesBeforeConfiguredSizeIsExceeded() throws IOException {
        Path log = tempDir.resolve("query.log");
        Logger logger = new Logger(log.toString(), 8);

        logger.append("1234");
        logger.append("5678");

        Assertions.assertTrue(Files.exists(Path.of(log + ".1")));
        Assertions.assertEquals("5678\n", Files.readString(log, StandardCharsets.UTF_8));
    }

    @Test
    void redactsSqlLiteralsAndCommonSecretAssignments() {
        String sanitized = SqlLogSanitizer.sanitizeQuery(
                "SELECT * FROM users WHERE email='person@example.test' AND id=123 AND token='abc'"
        );

        Assertions.assertFalse(sanitized.contains("person@example.test"));
        Assertions.assertFalse(sanitized.contains("123"));
        Assertions.assertFalse(sanitized.contains("abc"));
        Assertions.assertTrue(sanitized.contains("email='***'"));
        Assertions.assertTrue(sanitized.contains("id=?"));

        Assertions.assertEquals(
                "password=***; token: ***",
                SqlLogSanitizer.sanitizeMessage("password=secret; token: bearer-value")
        );
    }
}
