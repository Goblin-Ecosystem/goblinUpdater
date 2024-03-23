package updater.api.metrics;

/**
 * Interface for values associated to elements in dependency update
 */
public interface Metric {
    /**
     * Returns the type of this Metric.
     */
    MetricType type();

    /**
     * Computes the value of this Metric.
     */
    double compute();
}
