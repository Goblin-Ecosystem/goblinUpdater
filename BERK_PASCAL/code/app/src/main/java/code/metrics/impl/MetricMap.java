package code.metrics.impl;

import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.Set;

import code.metrics.api.Metric;
import code.metrics.api.MetricContainer;

public class MetricMap implements MetricContainer {
    private Map<Metric, Double> map;

    public MetricMap(Map<Metric, Double> map) {
        this.map = new HashMap<>(map);
    }

    @Override
    public Set<Metric> usedMetrics() {
        return map.keySet();
    }

    @Override
    public void addMetric(Metric m, Double value) {
        map.put(m, value);
    }

    @Override
    public Optional<Double> getValue(Metric metric) {
        return Optional.ofNullable(map.get(metric));
    }
}
