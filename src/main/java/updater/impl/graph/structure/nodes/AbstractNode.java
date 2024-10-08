package updater.impl.graph.structure.nodes;

import updater.api.graph.structure.UpdateNode;
import updater.api.metrics.MetricContainer;
import updater.api.metrics.MetricType;
import updater.impl.metrics.MetricMap;

import java.util.Optional;
import java.util.Set;
import java.util.Map;

public abstract class AbstractNode implements UpdateNode {
    private final String id;
    private final MetricContainer<MetricType> metricMap;

    protected AbstractNode(String id, Map<MetricType, Double> metricMap) {
        if (!hasValidId(id)) {
            throw new IllegalArgumentException("Invalid id: " + id);
        }
        this.id = id;
        this.metricMap = new MetricMap<>(metricMap);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Set<MetricType> contentTypes() {
        return metricMap.contentTypes();
    }

    @Override
    public void put(MetricType m, Double value) {
        metricMap.put(m, value);
    }

    @Override
    public Optional<Double> get(MetricType m) {
        return metricMap.get(m);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractNode other = (AbstractNode) obj;
        if (id == null) {
            return other.id == null;
        } else return id.equals(other.id);
    }

}
