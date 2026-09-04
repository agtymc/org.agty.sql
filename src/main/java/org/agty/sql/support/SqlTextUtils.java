package org.agty.sql.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Internal text/escaping helpers used by current production code.
 */
public final class SqlTextUtils {
    private static final Pattern NUMERIC_ENTITY = Pattern.compile("&#(\\d+);");

    private SqlTextUtils() {
    }

    public static String hencode(String source) {
        if (source == null || source.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int offset = 0; offset < source.length();) {
            int codePoint = source.codePointAt(offset);
            offset += Character.charCount(codePoint);
            switch (codePoint) {
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '&' -> sb.append("&amp;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&apos;");
                case '\\' -> sb.append("&#92;");
                case '\n' -> sb.append("&#10;");
                case '\r' -> sb.append("&#13;");
                case '\t' -> sb.append("&#9;");
                case '\0' -> sb.append("&#0;");
                default -> {
                    if (codePoint > 0x7f) {
                        sb.append("&#").append(codePoint).append(';');
                    } else {
                        sb.appendCodePoint(codePoint);
                    }
                }
            }
        }

        return sb.toString();
    }

    public static String hdecode(String body) {
        if (body == null || body.isEmpty()) {
            return "";
        }
        String decoded = body.replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">");

        Matcher matcher = NUMERIC_ENTITY.matcher(decoded);
        StringBuffer result = new StringBuffer(decoded.length());
        while (matcher.find()) {
            String replacement;
            try {
                int codePoint = Integer.parseInt(matcher.group(1));
                replacement = Character.isValidCodePoint(codePoint)
                        ? new String(Character.toChars(codePoint))
                        : matcher.group();
            } catch (NumberFormatException exception) {
                replacement = matcher.group();
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString().replace("&amp;", "&");
    }

    public static String removeUnsupportedChars(String query) {
        return query.replaceAll("\0", "");
    }
}
