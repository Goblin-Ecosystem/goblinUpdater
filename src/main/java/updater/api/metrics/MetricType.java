package updater.api.metrics;

// FIXME: abstraction depends on implementations
import updater.impl.metrics.Cve;
import updater.impl.metrics.CveAggregated;
import updater.impl.metrics.Freshness;
import updater.impl.metrics.FreshnessAggregated;

/**
 * Enum for metric types. Adding a new enum value will require adding:
 * <OL>
 * <LI>A new enum value to the enum
 * <LI>A new class that implements {@link Metric} and update metricClass
 * accordingly
 * <LI>Update the code for other methods too, accordingly
 * </OL>
 * This should be done in concordance with Goblin's Weaver code for metrics that are not computed (but obtained from a weaving).
 */
public enum MetricType {
    CVE,
    CVE_AGGREGATED,
    FRESHNESS,
    FRESHNESS_AGGREGATED,
    POPULARITY,
    POPULARITY_AGGREGATED,
    COST;

    /**
     * Returns the class that implements {@link Metric} for this enum value and can be used to compute its value.
     * FIXME: add classes for missing enum values
     * TODO: use prototype objects instead of classes
     */
    public Class<? extends Metric> metricClass() {
        return switch (this) {
            case CVE -> Cve.class;
            case CVE_AGGREGATED -> CveAggregated.class;
            case FRESHNESS -> Freshness.class;
            case FRESHNESS_AGGREGATED -> FreshnessAggregated.class;
            default -> null;
        };
    }

    /**
     * Returns true if the enum value corresponds to an aggregated value, false
     * otherwise
     */
    public boolean isAggregated() {
        return switch (this) {
            case CVE_AGGREGATED, FRESHNESS_AGGREGATED, POPULARITY_AGGREGATED -> true;
            default -> false;
        };
    }

    /**
     * Returns the enum value corresponding to the aggregated version of this enum
     * value, or this enum value if it is not an aggregated value
     */
    public MetricType aggregatedVersion() {
        return switch (this) {
            case CVE -> CVE_AGGREGATED;
            case FRESHNESS -> FRESHNESS_AGGREGATED;
            case POPULARITY -> POPULARITY_AGGREGATED;
            default -> this;
        };
    }

    /**
     * Returns the enum value corresponding to the non-aggregated version of this
     * enum value, or this enum value if it is not an aggregated value
     */
    public MetricType notAggregatedVersion() {
        return switch (this) {
            case CVE_AGGREGATED -> CVE;
            case FRESHNESS_AGGREGATED -> FRESHNESS;
            case POPULARITY_AGGREGATED -> POPULARITY;
            default -> this;
        };
    }

    /**
     * Returns true if the enum value corresponds to a quality metric.
     */
    public boolean isQualityMetric() {
        return switch (this) {
            case CVE, CVE_AGGREGATED, FRESHNESS, FRESHNESS_AGGREGATED, POPULARITY, POPULARITY_AGGREGATED -> true;
            default -> false;
         };
    }

    /**
     * Returns true if the enum value corresponds to a cost metric.
     */
    public boolean isCostMetric() {
        return switch (this) {
            case COST -> true;
            default -> false;
         };
    }

    /**
     * Returns the key used to get the value from JSON, e.g. "cve" for CVE and
     * "freshness" for FRESHNESS
     */
    public String getJsonKey() {
        return this.name().toLowerCase();
    }

}
