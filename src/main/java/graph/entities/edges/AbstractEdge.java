package graph.entities.edges;

import java.util.Optional;
import java.util.Set;
import java.util.Map;

import org.jgrapht.graph.DefaultEdge;

import addedvalue.AddedValueEnum;
import addedvalue.MetricContainer;
import addedvalue.MetricMap;

public abstract class AbstractEdge extends DefaultEdge implements UpdateEdge {

    private String id;
    // FIXME: not DRY wrt AbstractNode, can be extracted in a AbstractGraphElementWithMetricContainer.
    private MetricContainer<AddedValueEnum> metricMap;

    protected AbstractEdge(String id, Map<AddedValueEnum, Double> metricMap) {
        super();
        this.id = id;
        this.metricMap = new MetricMap<>(metricMap);
    }

    @Override
    public String id() {
        return this.id;
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

    @Override
    public String targetVersion() {
        return null;
    }

}
