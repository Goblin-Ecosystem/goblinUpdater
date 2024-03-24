package updater.api.metrics;

import java.util.Set;
import java.util.Optional;

/**
 * Interface for objects that can hold metrics types and their values.
 */
public interface MetricContainer<T> {
    /**
     * Returns the set of all metrics types in this container. Empty set if there are none.
     * 
     * @return the set of all metrics types in this container
     */
    Set<T> contentTypes();

    /**
     * Adds a new metric to the container. If the metric already exists in the
     * container, its value is updated.
     * 
     * @param m     the metric to be added
     * @param value the value of the metric
     */
    void put(T m, Double value);

    /**
     * Returns the value of a given metric in this container. If the metric does not
     * exist in the container, an empty optional is returned.
     * 
     * @param metric the metric to be queried
     * @return the value of the metric if it exists, otherwise an empty optional
     */
    Optional<Double> get(T metric);
}
