package code.metrics.api;

import java.util.Set;
import java.util.Optional;

public interface MetricContainer {
    Set<Metric> usedMetrics();

    void addMetric(Metric m, Double value);

    Optional<Double> getValue(Metric metric);
}
