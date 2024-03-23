package graph.entities.edges;

import java.util.Map;

import addedvalue.AddedValueEnum;

public class ChangeEdge extends AbstractEdge {
    private double qualityChange;
    private double changeCost;

    public ChangeEdge(String id, Map<AddedValueEnum, Double> metricMap) {
        super(id, metricMap);
    }

    private double getQualityChange() {
        return qualityChange;
    }

    // FIXME: should be private or in an interface
    public void setQualityChange(double qualityChange) {
        this.qualityChange = qualityChange;
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
