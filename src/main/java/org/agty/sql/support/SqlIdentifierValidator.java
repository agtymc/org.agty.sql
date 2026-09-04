package org.agty.sql.support;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Validates structural SQL identifiers before dialect rendering.
 */
public final class SqlIdentifierValidator {
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private SqlIdentifierValidator() {}

    /**
     * Validates a simple or qualified table name.
     *
     * @param table table identifier
     * @return the unchanged validated identifier
     */
    public static String requireTable(String table) {
        requireNonBlank(table, "table");
        requireQualifiedIdentifier(table, "table", true, false);
        return table;
    }

    /**
     * Validates a simple column name.
     *
     * @param column column identifier
     * @param role identifier role used in error messages
     * @return the unchanged validated identifier
     */
    public static String requireColumn(String column, String role) {
        requireNonBlank(column, role);
        if (!IDENTIFIER.matcher(column).matches()) {
            throw invalid(role);
        }
        return column;
    }

    /**
     * Validates a comma-separated select/returning field list.
     *
     * @param fields field list
     * @return the unchanged validated field list
     */
    public static String requireFieldList(String fields) {
        requireNonBlank(fields, "fields");
        for (String field : fields.split(",", -1)) {
            String item = field.trim();
            if (item.equals("*")) {
                continue;
            }
            if (item.endsWith(".*")) {
                requireQualifiedIdentifier(
                        item.substring(0, item.length() - 2),
                        "fields",
                        true,
                        true
                );
                continue;
            }
            requireQualifiedIdentifier(item, "fields", true, true);
        }
        return fields;
    }

    /**
     * Validates a comma-separated GROUP BY identifier list.
     *
     * @param groupBy grouping list
     * @return the unchanged validated grouping list
     */
    public static String requireGroupBy(String groupBy) {
        requireNonBlank(groupBy, "GROUP BY");
        for (String field : groupBy.split(",", -1)) {
            requireQualifiedIdentifier(field.trim(), "GROUP BY", true, true);
        }
        return groupBy;
    }

    /**
     * Validates ORDER BY identifiers and standard sort modifiers.
     *
     * @param orderBy sorting list
     * @return the unchanged validated sorting list
     */
    public static String requireOrderBy(String orderBy) {
        requireNonBlank(orderBy, "ORDER BY");
        for (String field : orderBy.split(",", -1)) {
            requireOrderItem(field.trim());
        }
        return orderBy;
    }

    private static void requireOrderItem(String item) {
        String[] parts = item.split("\\s+");
        if (parts.length == 0 || parts.length > 4) {
            throw invalid("ORDER BY");
        }

        requireQualifiedIdentifier(parts[0], "ORDER BY", true, true);

        int index = 1;
        if (index < parts.length && isOneOf(parts[index], "ASC", "DESC")) {
            index++;
        }
        if (index < parts.length && isOneOf(parts[index], "NULLS")) {
            index++;
            if (index >= parts.length || !isOneOf(parts[index], "FIRST", "LAST")) {
                throw invalid("ORDER BY");
            }
            index++;
        }
        if (index != parts.length) {
            throw invalid("ORDER BY");
        }
    }

    private static boolean isOneOf(String value, String... expected) {
        String upperValue = value.toUpperCase(Locale.ROOT);
        for (String item : expected) {
            if (upperValue.equals(item)) {
                return true;
            }
        }
        return false;
    }

    private static void requireQualifiedIdentifier(
            String value,
            String role,
            boolean allowTableMarker,
            boolean allowColumnMarker
    ) {
        requireNonBlank(value, role);
        for (String segment : value.split("\\.", -1)) {
            String identifier = unwrap(segment, allowTableMarker, allowColumnMarker);
            if (!IDENTIFIER.matcher(identifier).matches()) {
                throw invalid(role);
            }
        }
    }

    private static String unwrap(
            String segment,
            boolean allowTableMarker,
            boolean allowColumnMarker
    ) {
        if (allowTableMarker && segment.startsWith("{") && segment.endsWith("}")) {
            return segment.substring(1, segment.length() - 1);
        }
        if (allowColumnMarker && segment.startsWith("[") && segment.endsWith("]")) {
            return segment.substring(1, segment.length() - 1);
        }
        return segment;
    }

    private static void requireNonBlank(String value, String role) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SQL " + role + " must not be null or blank");
        }
    }

    private static IllegalArgumentException invalid(String role) {
        return new IllegalArgumentException("Invalid SQL identifier in " + role);
    }
}
