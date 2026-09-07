package org.agty.sql.driver;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** Immutable, extensible feature matrix for one SQL dialect. */
public final class DialectFeatureSet {
    private static final DialectFeatureSet NONE = new DialectFeatureSet(
            new EnumMap<>(DialectFeature.class)
    );

    private final Map<DialectFeature, DialectFeatureSupport> support;

    private DialectFeatureSet(EnumMap<DialectFeature, DialectFeatureSupport> support) {
        this.support = Collections.unmodifiableMap(new EnumMap<>(support));
    }

    /**
     * Returns an empty feature set where every feature is unsupported.
     *
     * @return empty feature set
     */
    public static DialectFeatureSet none() {
        return NONE;
    }

    /**
     * Starts construction of a feature set.
     *
     * @return mutable builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the support level for a feature.
     *
     * @param feature feature to inspect
     * @return configured level, or {@link DialectFeatureSupport#UNSUPPORTED}
     */
    public DialectFeatureSupport support(DialectFeature feature) {
        Objects.requireNonNull(feature, "feature");
        return support.getOrDefault(feature, DialectFeatureSupport.UNSUPPORTED);
    }

    /**
     * Returns whether a feature is available in a documented form.
     *
     * @param feature feature to inspect
     * @return whether the feature is supported
     */
    public boolean supports(DialectFeature feature) {
        return support(feature).isSupported();
    }

    /**
     * Returns a read-only snapshot containing explicitly configured features.
     *
     * @return immutable feature map
     */
    public Map<DialectFeature, DialectFeatureSupport> asMap() {
        return support;
    }

    /** Builder that defaults omitted features to unsupported. */
    public static final class Builder {
        private final EnumMap<DialectFeature, DialectFeatureSupport> support =
                new EnumMap<>(DialectFeature.class);

        private Builder() {
        }

        /**
         * Defines the support level of one feature.
         *
         * @param feature feature to configure
         * @param level support level
         * @return this builder
         */
        public Builder support(DialectFeature feature, DialectFeatureSupport level) {
            support.put(
                    Objects.requireNonNull(feature, "feature"),
                    Objects.requireNonNull(level, "level")
            );
            return this;
        }

        /**
         * Creates an immutable feature set.
         *
         * @return configured feature set
         */
        public DialectFeatureSet build() {
            return support.isEmpty() ? NONE : new DialectFeatureSet(support);
        }
    }
}
