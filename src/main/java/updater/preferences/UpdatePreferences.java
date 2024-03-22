package updater.preferences;

import addedvalue.AddedValueEnum;

import java.util.HashSet;
import java.util.Set;

public interface UpdatePreferences {
    Set<AddedValueEnum> qualityMetrics();

    Set<AddedValueEnum> qualityMetricsAggregated();

    Set<AddedValueEnum> costMetrics();

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

    default boolean isValid() {
        // 1. at least one quality metric
        if (qualityMetrics().isEmpty())
            return false;
        // 2. at least one cost metric
        if (costMetrics().isEmpty())
            return false;
        // 3. the sum of coefficients for quality metrics is 1
        if (sumFor(qualityMetrics()) != 1)
            return false;
        // 4. the sum of coefficients for cost metrics is 1
        if (sumFor(costMetrics()) != 1)
            return false;
        return true;
    }
}
