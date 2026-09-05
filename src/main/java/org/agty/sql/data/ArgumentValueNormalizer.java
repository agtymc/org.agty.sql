package org.agty.sql.data;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Validates dynamically supplied argument values without implicit string conversion.
 */
final class ArgumentValueNormalizer {
    private static final String SUPPORTED_DATA_TYPES = "String, Number, Boolean, Character";

    private ArgumentValueNormalizer() {
    }

    static <T> T requireType(String field, Object value, Class<T> expectedType) {
        if (value == null) {
            return null;
        }
        if (!expectedType.isInstance(value)) {
            throw new IllegalArgumentException(
                    "Invalid data type for field '%s': expected %s, got %s".formatted(
                            field,
                            expectedType.getSimpleName(),
                            value.getClass().getName()
                    )
            );
        }
        return expectedType.cast(value);
    }

    static Number normalizeNumber(String field, Number value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Float || value instanceof Double) {
            validateFiniteNumber(field, value);
        }
        if (value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                || value instanceof Long
                || value instanceof Float
                || value instanceof Double
                || value instanceof BigInteger
                || value instanceof BigDecimal) {
            return value;
        }

        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Unsupported Number implementation for field '%s': %s".formatted(
                            field,
                            value.getClass().getName()
                    ),
                    exception
            );
        }
    }

    static void validateFiniteNumber(String field, Number value) {
        if (value instanceof Float floatValue && !Float.isFinite(floatValue)) {
            throw invalidDecimalValue(field, value);
        }
        if (value instanceof Double doubleValue && !Double.isFinite(doubleValue)) {
            throw invalidDecimalValue(field, value);
        }
    }

    static IllegalArgumentException unsupportedDataType(String field, Object value) {
        return new IllegalArgumentException(
                "Unsupported data type for field '%s': %s. Supported types: %s".formatted(
                        field,
                        value.getClass().getName(),
                        SUPPORTED_DATA_TYPES
                )
        );
    }

    private static IllegalArgumentException invalidDecimalValue(String field, Number value) {
        return new IllegalArgumentException(
                "Invalid decimal value for field '%s': %s is not finite".formatted(field, value)
        );
    }
}
