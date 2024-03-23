package graph.entities.edges;

import java.util.Map;

import updater.api.metrics.MetricType;

public class VersionEdge extends AbstractEdge {

    public VersionEdge(String id, Map<MetricType, Double> metricMap) {
        super(id, metricMap);
    }

    @Override
    public boolean isVersion() {
        return true;
    }

    @Override
    public boolean isDependency() {
        return false;
    }

    @Override
    public boolean isChange() {
        return false;
    }
}
