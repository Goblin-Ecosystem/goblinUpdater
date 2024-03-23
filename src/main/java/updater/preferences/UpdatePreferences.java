package updater.preferences;

import addedvalue.AddedValueEnum;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public interface UpdatePreferences {
    Set<AddedValueEnum> qualityMetrics();

    Set<AddedValueEnum> qualityMetricsAggregated();

    Set<AddedValueEnum> costMetrics();

    /**
     * Get the coefficient for a given metric. 0.0 if the metric is not in the preferences.
     * @param addedValueEnum
     * @return
     */
    double coefficientFor(AddedValueEnum addedValueEnum);

    default Set<AddedValueEnum> metrics() {
        Set<AddedValueEnum> metrics = new HashSet<>(qualityMetrics());
        metrics.addAll(costMetrics());
        return metrics;
    }

    default Double sumFor(Set<AddedValueEnum> addedValues) {
        return addedValues.stream()
                .mapToDouble(this::coefficientFor)
                .sum();
    }

    // TODO: use Either<String, Void> ?
    default boolean isValid() {
        // 1. at least one quality metric
        if (qualityMetrics().isEmpty()) {
            System.out.println("Missing quality metrics");
            return false;
        }
        // 2. at least one cost metric
        if (costMetrics().isEmpty()) {
            System.out.println("Missing cost metrics");
            return false;
        }
        // 3. the sum of coefficients for quality metrics is 1
        if (sumFor(qualityMetrics()) != 1.0) {
            System.out.println("Quality weights sum should be 1.0");
            return false;
        }
        return true;
    }
}
