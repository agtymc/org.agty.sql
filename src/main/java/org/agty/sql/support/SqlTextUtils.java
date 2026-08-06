package org.agty.sql.support;

/**
 * Internal text/escaping helpers used by current production code.
 */
public final class SqlTextUtils {

    private SqlTextUtils() {
    }

    public static String hencode(String source) {
        if (source == null || source.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            switch (c) {
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '&' -> sb.append("&amp;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&apos;");
                default -> sb.append(c);
            }
        }

        return sb.toString();
    }

    public static String hdecode(String body) {
        if (body == null || body.isEmpty()) {
            return "";
        }
        return body.replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&#039;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">");
    }

    public static String removeUnsupportedChars(String query) {
        return query.replaceAll("\0", "");
    }
}
