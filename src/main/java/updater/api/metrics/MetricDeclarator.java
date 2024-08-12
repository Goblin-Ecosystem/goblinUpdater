package updater.api.metrics;

import java.util.Optional;

/**
 * Interface for objects used to bind at runtime a metric with a class to
 * compute its value.
 */
public interface MetricDeclarator {
    /**
     * Returns the metric bound to the given metric type if any.
     * 
     * @param type The metric type to look for.
     * @return The metric bound to the given metric type or empty optional
     *         otherwise.
     */
    Optional<Class<? extends Metric>> metric(MetricType type);

    /**
     * Returns the JSON key corresponding to the given metric type.
     * @param type
     * @return The JSON key corresponding to the given metric type.
     */
    default String toJsonKey(MetricType type) {
        return type.toJsonKey();
    }

    /**
     * Returns the metric type corresponding to the given JSON key, if any.
     * @param jsonKey The JSON key of the metric type to look for.
     * @return The metric type corresponding to the given JSON key or empty
     */
    Optional<MetricType> fromJsonKey(String jsonKey);

}
