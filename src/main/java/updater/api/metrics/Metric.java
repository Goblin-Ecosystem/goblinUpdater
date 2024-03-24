package updater.api.metrics;

/**
 * Interface for values associated to elements in dependency graphs works.
 */
public interface Metric {
    /**
     * Returns the type of this Metric.
     */
    MetricType type();

    /**
     * Computes the value of this Metric. This can correspond to a real computation, a direct retrieve from a weaving output, or a combination of both.
     */
    double compute();
}
