package updater.api.metrics;

/**
 * Inteface for metric types.
 */
public interface MetricType {
    /**
     * Returns true if the enum value corresponds to an aggregated value, false
     * otherwise
     */
    boolean isAggregated();

    /**
     * Returns the enum value corresponding to the aggregated version of this enum
     * value, or this enum value if it is not an aggregated value
     */
    MetricType aggregatedVersion();

    /**
     * Returns the enum value corresponding to the non-aggregated version of this
     * enum value, or this enum value if it is not an aggregated value
     */
    MetricType nonAggregatedVersion();

    /**
     * Returns true if the enum value corresponds to a quality metric.
     */
    boolean isQualityMetric();

    /**
     * Returns true if the enum value corresponds to a cost metric.
     */
    boolean isCostMetric();

    /**
     * Returns the key used to get the value from JSON, e.g. "cve" for CVE and
     * "freshness" for FRESHNESS
     */
    String toJsonKey();

}
