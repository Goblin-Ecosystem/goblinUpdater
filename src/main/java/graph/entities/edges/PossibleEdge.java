package graph.entities.edges;

public class PossibleEdge extends JgraphtCustomEdge {
    public double qualityChange;
    public double changeCost;

    public PossibleEdge() {
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

    @Override
    public boolean isVersion() {
        return false;
    }

    @Override
    public boolean isDependency() {
        return false;
    }

    @Override
    public boolean isPossible() {
        return true;
    }
}
