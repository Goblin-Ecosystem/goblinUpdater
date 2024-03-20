package graph.entities.edges;

public class ChangeEdge extends AbstractEdge {
    private double qualityChange;
    private double changeCost;

    public ChangeEdge(String id) {
        super(id);
    }

    public double getQualityChange() {
        return qualityChange;
    }

    public void setQualityChange(double qualityChange) {
        this.qualityChange = qualityChange;
    }

    public double getChangeCost() {
        return changeCost;
    }

    public void setChangeCost(double changeCost) {
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
