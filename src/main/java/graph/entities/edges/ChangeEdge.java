package graph.entities.edges;

public class ChangeEdge extends AbstractEdge {
    private double qualityChange;
    private double changeCost;

    public ChangeEdge(String id) {
        super(id);
    }

    private double getQualityChange() {
        return qualityChange;
    }

    // FIXME: should be private or in an interface
    public void setQualityChange(double qualityChange) {
        this.qualityChange = qualityChange;
    }

    private double getChangeCost() {
        return changeCost;
    }

    // FIXME: should be private or in an interface
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
