package graph.entities.nodes;

import addedvalue.AddedValue;
import addedvalue.AddedValueEnum;
import addedvalue.MetricContainer;
import addedvalue.MetricMap;
import updater.preferences.UpdatePreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map;

public abstract class AbstractNode implements UpdateNode {
    private final String id;
    private MetricContainer<AddedValueEnum> metricMap;
    // FIXME: replaced by the MetricContainer API
    private final List<AddedValue> addedValues = new ArrayList<>();
    private Double quality = null;

    protected AbstractNode(String id, Map<AddedValueEnum, Double> metricMap) {
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
    public Set<AddedValueEnum> usedMetrics() {
        return metricMap.usedMetrics();
    }

    @Override
    public void addMetric(AddedValueEnum m, Double value) {
        metricMap.addMetric(m, value);
    }

    @Override
    public Optional<Double> getValue(AddedValueEnum m) {
        return metricMap.getValue(m);
    }

    // FIXME: should be private or in an interface
    // FIXME: replaced by the MetricContainer API?
    public void addAddedValue(AddedValue addedValue) {
        this.addedValues.add(addedValue);
    }

    // FIXME: should be private or in an interface
    // FIXME: replaced by the MetricContainer API?
    public double getNodeQuality(UpdatePreferences updatePreferences) {
        if (this.quality != null) {
            return this.quality;
        }
        this.quality = 0.0;
        for (AddedValue addedValue : addedValues) {
            this.quality += (addedValue.getQualityScore()
                    * updatePreferences.coefficientFor(addedValue.getAddedValueEnum())); // TODO: normalize quality + score
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
