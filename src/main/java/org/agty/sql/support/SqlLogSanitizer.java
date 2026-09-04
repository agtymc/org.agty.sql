package org.agty.sql.support;

import java.util.regex.Pattern;

/** Redacts values and common credentials before diagnostic text is persisted. */
public final class SqlLogSanitizer {
    private static final Pattern SECRET_ASSIGNMENT = Pattern.compile(
            "(?i)\\b(password|passwd|pwd|token|secret|authorization)\\b(\\s*[=:]\\s*)([^;,&\\s]+)"
    );

    private SqlLogSanitizer() {
    }

    /**
     * Redacts string, dollar-quoted, and numeric SQL literals.
     *
     * @param query query text
     * @return redacted text
     */
    public static String sanitizeQuery(String query) {
        if (query == null || query.isEmpty()) return query;

        StringBuilder result = new StringBuilder(query.length());
        for (int index = 0; index < query.length();) {
            char current = query.charAt(index);
            if (current == '\'') {
                result.append("'***'");
                index = skipQuoted(query, index, '\'');
                continue;
            }
            if (current == '$') {
                int nextIndex = redactDollarQuoted(query, result, index);
                if (nextIndex != index) {
                    index = nextIndex;
                    continue;
                }
            }
            if (Character.isDigit(current) && isNumericBoundary(query, index - 1)) {
                int end = skipNumber(query, index);
                if (isNumericBoundary(query, end)) {
                    result.append('?');
                    index = end;
                    continue;
                }
            }
            result.append(current);
            index++;
        }
        return sanitizeMessage(result.toString());
    }

    /**
     * Redacts common secret assignments in arbitrary diagnostic text.
     *
     * @param message diagnostic text
     * @return redacted text
     */
    public static String sanitizeMessage(String message) {
        if (message == null || message.isEmpty()) return message;
        return SECRET_ASSIGNMENT.matcher(message).replaceAll("$1$2***");
    }

    private static int skipQuoted(String value, int start, char quote) {
        int index = start + 1;
        while (index < value.length()) {
            char current = value.charAt(index++);
            if (current == '\\' && index < value.length()) {
                index++;
            } else if (current == quote) {
                if (index < value.length() && value.charAt(index) == quote) {
                    index++;
                } else {
                    break;
                }
            }
        }
        return index;
    }

    private static int redactDollarQuoted(String query, StringBuilder result, int start) {
        int delimiterEnd = query.indexOf('$', start + 1);
        if (delimiterEnd < 0 || !isDollarTag(query, start + 1, delimiterEnd)) return start;
        String delimiter = query.substring(start, delimiterEnd + 1);
        int contentEnd = query.indexOf(delimiter, delimiterEnd + 1);
        if (contentEnd < 0) return start;
        result.append(delimiter).append("***").append(delimiter);
        return contentEnd + delimiter.length();
    }

    private static boolean isDollarTag(String query, int start, int end) {
        if (start == end) return true;
        char first = query.charAt(start);
        if (!Character.isLetter(first) && first != '_') return false;
        for (int index = start + 1; index < end; index++) {
            char current = query.charAt(index);
            if (!Character.isLetterOrDigit(current) && current != '_') return false;
        }
        return true;
    }

    private static int skipNumber(String query, int start) {
        int index = start;
        while (index < query.length()) {
            char current = query.charAt(index);
            if (!Character.isDigit(current) && current != '.' && current != 'e' && current != 'E'
                    && current != '+' && current != '-') {
                break;
            }
            index++;
        }
        return index;
    }

    private static boolean isNumericBoundary(String query, int index) {
        if (index < 0 || index >= query.length()) return true;
        char value = query.charAt(index);
        return !Character.isLetterOrDigit(value) && value != '_' && value != '.';
    }
}
