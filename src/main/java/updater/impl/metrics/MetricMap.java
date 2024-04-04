package updater.impl.metrics;

import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.Set;

import updater.api.metrics.MetricContainer;
import updater.api.metrics.MetricType;

public class MetricMap<T extends MetricType> implements MetricContainer<T> {

    private Map<T, Double> map;

    public MetricMap(Map<T, Double> map) {
        if (map == null)
            this.map = new HashMap<>();
        else
            this.map = new HashMap<>(map);
    }

    @Override
    public Set<T> contentTypes() {
        return map.keySet();
    }

    public void put(T m, Double value) {
        map.put(m, value);
    }

    @Override
    public Optional<Double> get(T metric) {
        return Optional.ofNullable(map.get(metric));
    }

}
