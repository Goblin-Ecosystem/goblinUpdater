package graph.entities.edges;

public class ChangeEdge extends CustomEdge{
    public double qualityChange;
    public double changeCost;

    public ChangeEdge() {
        super(EdgeType.CHANGE);
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
}
