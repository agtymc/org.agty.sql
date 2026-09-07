package org.agty.sql.driver;

/** Describes how a dialect exposes a {@link DialectFeature}. */
public enum DialectFeatureSupport {
    /** The engine provides the expected feature directly. */
    SUPPORTED,
    /** The engine provides the capability through different SQL syntax. */
    ALTERNATIVE_SYNTAX,
    /** The capability is provided through the JDBC driver contract. */
    JDBC_DRIVER,
    /** The capability is not available through the current dialect contract. */
    UNSUPPORTED;

    /**
     * Returns whether the feature can be used in some documented form.
     *
     * @return {@code true} unless the feature is unsupported
     */
    public boolean isSupported() {
        return this != UNSUPPORTED;
    }

    /**
     * Returns whether portable callers need dialect-specific SQL syntax.
     *
     * @return {@code true} for alternative syntax
     */
    public boolean requiresAlternativeSyntax() {
        return this == ALTERNATIVE_SYNTAX;
    }
}
