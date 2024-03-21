package addedvalue;

import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.Set;

public class MetricMap<T> implements MetricContainer<T> {

    private Map<T, Double> map;

    public MetricMap(Map<T, Double> map) {
        map = new HashMap<>(map);
    }

    @Override
    public Set<T> usedMetrics() {
        return map.keySet();
    }

    @Override
    public void addMetric(T m, Double value) {
        map.put(m, value);
    }

    @Override
    public Optional<Double> getValue(T metric) {
        return Optional.ofNullable(map.get(metric));
    }

}
