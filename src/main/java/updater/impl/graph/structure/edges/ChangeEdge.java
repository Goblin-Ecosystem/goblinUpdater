package updater.impl.graph.structure.edges;

import java.util.Map;

import updater.api.metrics.MetricType;

public class ChangeEdge extends AbstractEdge {
    private double changeCost;

    public ChangeEdge(String id, Map<MetricType, Double> metricMap) {
        super(id, metricMap);
    }

    // FIXME: should be private or in an interface
    public  double cost() {
        return changeCost;
    }

    // FIXME: should be private or in an interface
    public void setCost(double changeCost) {
        this.changeCost = changeCost;
    }

    @Override
    public boolean isVersion() {
        return false;
    }

    @Override
    public boolean isDependency() {
        return false;
    }

    @Override
    public boolean isChange() {
        return true;
    }
}
