package updater.api.preferences;

import java.util.HashSet;
import java.util.Set;

import updater.api.metrics.MetricType;
import util.helpers.system.LoggerHelpers;

/**
 * Interface for update preferences.
 */
public interface Preferences {
    /**
     * Get the set of quality metrics that are of interest for the user.
     * 
     * @return the set of quality metrics that are of interest for the user.
     */
    Set<MetricType> qualityMetrics();

    /**
     * Get the set of cost metrics that are of interest for the user.
     * 
     * @return the set of cost metrics that are of interest for the user.
     */
    Set<MetricType> costMetrics();

    /**
     * Get all metrics that are of interest for the user. This is the union of the
     * quality metrics and the cost metrics.
     * 
     * @return the set of all metrics that are of interest for the user.
     */
    default Set<MetricType> metrics() {
        Set<MetricType> metrics = new HashSet<>(qualityMetrics());
        metrics.addAll(costMetrics());
        return metrics;
    }

    /**
     * Get the coefficient for a given metric. 0.0 if the metric is not in the
     * preferences (ie the metric has weight 0 for the user).
     * 
     * @param metric
     * @return
     */
    double coefficientFor(MetricType metric);

    /**
     * Get the sum of all coefficients for a given set of added values.
     */
    default Double sumFor(Set<MetricType> metrics) {
        return metrics.stream()
                .mapToDouble(this::coefficientFor)
                .sum();
    }

    /**
     * Checks whether the preferences are valid or not. Validity means that there is
     * at least one quality metric, at list one cost metric and that the sum of
     * coefficients for quality metrics is 1.
     * 
     * @return true if valid, false otherwise
     */
    default boolean isValid() {
        // 1. at least one quality metric
        if (qualityMetrics().isEmpty()) {
            LoggerHelpers.error("Missing quality metrics");
            return false;
        }
        // 2. at least one cost metric
        if (costMetrics().isEmpty()) {
            LoggerHelpers.error("Missing cost metrics");
            return false;
        }
        // 3. the sum of coefficients for quality metrics is 1
        if (sumFor(qualityMetrics()) != 1.0) {
            LoggerHelpers.error("Quality weights sum should be 1.0");
            return false;
        }
        return true;
    }
}
