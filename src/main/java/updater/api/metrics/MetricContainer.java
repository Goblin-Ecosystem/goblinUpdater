package updater.api.metrics;

import updater.api.preferences.Preferences;

import java.util.Set;
import java.util.Optional;

/**
 * Interface for objects that can hold metrics types and their values.
 */
public interface MetricContainer<T extends MetricType> {
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

    /**
     * Return the aggregated values of metrics with the coefficients associated for each metric.
     * @param updatePreferences user preferences (for metrics coefficients).
     * @return aggregated quality metrics values.
     */
    default double getQuality(Preferences updatePreferences){
        double quality = 0.0;
        for (T metricType : contentTypes().stream().filter(MetricType::isQualityMetric).toList()) {
            Optional<Double> metricValue = get(metricType);
            Optional<Double> coefficient = updatePreferences.coefficientFor(metricType);
            if(metricValue.isPresent() && coefficient.isPresent()){
                quality += (metricValue.get() * coefficient.get());
            }
        }
        return quality;
    }
}
