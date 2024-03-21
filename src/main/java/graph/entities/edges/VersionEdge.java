package graph.entities.edges;

import java.util.Map;

import addedvalue.AddedValueEnum;

public class VersionEdge extends AbstractEdge {

    public VersionEdge(String id, Map<AddedValueEnum, Double> metricMap) {
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
