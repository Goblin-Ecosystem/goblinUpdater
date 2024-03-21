package addedvalue;

import java.util.Set;
import java.util.Optional;

public interface MetricContainer<T> {
    Set<T> usedMetrics();

    void addMetric(T m, Double value);

    Optional<Double> getValue(T metric);
}
