package org.agty.sql.support;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Formats the deliberately retained legacy WHERE placeholders without invoking
 * arbitrary {@code Object.toString()} implementations.
 */
public final class LegacySqlFormatter {

    private LegacySqlFormatter() {
    }

    public static String format(String template, Object... arguments) {
        if (template == null) {
            throw new IllegalArgumentException("Legacy SQL template must not be null");
        }

        StringBuilder result = new StringBuilder(template.length());
        boolean inStringLiteral = false;
        int argumentIndex = 0;

        for (int index = 0; index < template.length(); index++) {
            char current = template.charAt(index);
            if (current == '\'' && inStringLiteral
                    && index + 1 < template.length()
                    && template.charAt(index + 1) == '\'') {
                result.append("''");
                index++;
                continue;
            }
            if (current == '\'') {
                inStringLiteral = !inStringLiteral;
                result.append(current);
                continue;
            }
            if (current != '%') {
                result.append(current);
                continue;
            }
            if (index + 1 >= template.length()) {
                throw unsupportedPlaceholder(template, index);
            }

            char conversion = template.charAt(++index);
            if (conversion == '%') {
                result.append('%');
                continue;
            }
            if (argumentIndex >= arguments.length) {
                throw new IllegalArgumentException("Not enough values for legacy SQL placeholders");
            }

            Object value = arguments[argumentIndex++];
            result.append(render(conversion, value, inStringLiteral));
        }

        if (argumentIndex != arguments.length) {
            throw new IllegalArgumentException("Too many values for legacy SQL placeholders");
        }
        return result.toString();
    }

    private static String render(char conversion, Object value, boolean inStringLiteral) {
        return switch (conversion) {
            case 's' -> renderString(value, inStringLiteral);
            case 'd' -> renderInteger(value, inStringLiteral);
            case 'f' -> renderDecimal(value, inStringLiteral);
            case 'b' -> renderBoolean(value, inStringLiteral);
            default -> throw new IllegalArgumentException(
                    "Unsupported legacy SQL placeholder: %" + conversion
                            + ". Supported placeholders: %s, %d, %f, %b, %%"
            );
        };
    }

    private static String renderString(Object value, boolean inStringLiteral) {
        if (!inStringLiteral) {
            throw new IllegalArgumentException(
                    "Legacy %s values must be enclosed in SQL single quotes; "
                            + "use prepared mode for new code"
            );
        }
        if (value == null) {
            throw new IllegalArgumentException(
                    "Legacy %s does not accept null; use IS NULL or prepared mode"
            );
        }
        if (!(value instanceof String) && !(value instanceof Character)) {
            throw invalidType("%s", value, "String or Character");
        }
        return SqlTextUtils.hencode(value.toString());
    }

    private static String renderInteger(Object value, boolean inStringLiteral) {
        requireOutsideStringLiteral("%d", inStringLiteral);
        if (!(value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                || value instanceof Long
                || value instanceof BigInteger)) {
            throw invalidType("%d", value, "an integer Number");
        }
        return value.toString();
    }

    private static String renderDecimal(Object value, boolean inStringLiteral) {
        requireOutsideStringLiteral("%f", inStringLiteral);
        if (!(value instanceof Number number)) {
            throw invalidType("%f", value, "Number");
        }
        if (number instanceof Double doubleValue && !Double.isFinite(doubleValue)
                || number instanceof Float floatValue && !Float.isFinite(floatValue)) {
            throw new IllegalArgumentException("Legacy %f does not accept non-finite values");
        }

        try {
            return value instanceof BigDecimal ? value.toString() : new BigDecimal(value.toString()).toString();
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Legacy %f requires a numeric decimal representation", exception);
        }
    }

    private static String renderBoolean(Object value, boolean inStringLiteral) {
        requireOutsideStringLiteral("%b", inStringLiteral);
        if (!(value instanceof Boolean)) {
            throw invalidType("%b", value, "Boolean");
        }
        return value.toString();
    }

    private static void requireOutsideStringLiteral(String placeholder, boolean inStringLiteral) {
        if (inStringLiteral) {
            throw new IllegalArgumentException(placeholder + " must not be enclosed in SQL single quotes");
        }
    }

    private static IllegalArgumentException invalidType(String placeholder, Object value, String expected) {
        String actual = value == null ? "null" : value.getClass().getName();
        return new IllegalArgumentException(
                "Invalid legacy SQL value for " + placeholder + ": expected " + expected + ", got " + actual
        );
    }

    private static IllegalArgumentException unsupportedPlaceholder(String template, int index) {
        return new IllegalArgumentException(
                "Incomplete or unsupported legacy SQL placeholder at index " + index + " in: " + template
        );
    }
}
