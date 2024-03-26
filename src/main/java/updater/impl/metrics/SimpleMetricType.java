package updater.impl.metrics;

import updater.api.metrics.Metric;
import updater.api.metrics.MetricDeclarator;
import updater.api.metrics.MetricType;

/**
 * Enum for metric types. Adding a new enum value will require adding:
 * <OL>
 * <LI>A new enum value to the enum
 * <LI>Update the code for methods, accordingly
 * <LI>A new class that implements {@link Metric} and update a
 * {@link MetricDeclarator}, accordingly
 * </OL>
 */
public enum SimpleMetricType implements MetricType {
    CVE,
    CVE_AGGREGATED,
    FRESHNESS,
    FRESHNESS_AGGREGATED,
    POPULARITY_1_YEAR,
    POPULARITY_1_YEAR_AGGREGATED,
    COST;

    public boolean isAggregated() {
        return switch (this) {
            case CVE_AGGREGATED, FRESHNESS_AGGREGATED, POPULARITY_1_YEAR_AGGREGATED -> true;
            default -> false;
        };
    }

    public MetricType aggregatedVersion() {
        return switch (this) {
            case CVE -> CVE_AGGREGATED;
            case FRESHNESS -> FRESHNESS_AGGREGATED;
            case POPULARITY_1_YEAR -> POPULARITY_1_YEAR_AGGREGATED;
            default -> this;
        };
    }

    public MetricType nonAggregatedVersion() {
        return switch (this) {
            case CVE_AGGREGATED -> CVE;
            case FRESHNESS_AGGREGATED -> FRESHNESS;
            case POPULARITY_1_YEAR_AGGREGATED -> POPULARITY_1_YEAR;
            default -> this;
        };
    }

    public boolean isQualityMetric() {
        return switch (this) {
            case CVE, CVE_AGGREGATED, FRESHNESS, FRESHNESS_AGGREGATED, POPULARITY_1_YEAR, POPULARITY_1_YEAR_AGGREGATED -> true;
            default -> false;
        };
    }

    public boolean isCostMetric() {
        return switch (this) {
            case COST -> true;
            default -> false;
        };
    }

    @Override
    public String toJsonKey() {
        return this.name().toLowerCase();
    }

}
