package updater.impl.graph.structure.nodes;

import updater.api.graph.structure.UpdateNode;
import updater.api.metrics.Metric;
import updater.api.metrics.MetricContainer;
import updater.api.metrics.MetricType;
import updater.api.preferences.Preferences;
import updater.impl.metrics.MetricMap;
import updater.impl.metrics.Popularity1Year;

import java.util.Optional;
import java.util.Set;
import java.util.Map;

public abstract class AbstractNode implements UpdateNode {
    private final String id;
    private MetricContainer<MetricType> metricMap;
    private Double quality = null;

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

    // FIXME: should be private or in an interface
    // FIXME: replaced by the MetricContainer API?
    public double getNodeQuality(Preferences updatePreferences) {
        if (this.quality != null) {
            return this.quality;
        }
        this.quality = 0.0;
        for (MetricType metricType : metricMap.contentTypes()) {
            Optional<Double> metricValue = metricMap.get(metricType);
            // TODO: normalize quality + score
            metricValue.ifPresent(value -> this.quality += (value * updatePreferences.coefficientFor(metricType)));
        }
        return this.quality;
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
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
