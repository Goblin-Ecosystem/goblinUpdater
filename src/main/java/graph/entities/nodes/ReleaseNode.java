package graph.entities.nodes;

import updater.updatePreferences.UpdatePreferences;

public class ReleaseNode extends NodeObject {
    private long timestamp = 0;
    private String version = "";
    private double changeCost = 0.0;

    public ReleaseNode(String id) {
        super(id, NodeType.RELEASE);
    }

    public ReleaseNode(String id, long timestamp, String version) {
        super(id, NodeType.RELEASE);
        this.timestamp = timestamp;
        this.version = version;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getVersion() {
        return version;
    }

    public double getChangeCost() {
        return changeCost;
    }

    public void setChangeCost(double changeCost) {
        this.changeCost = changeCost;
    }

    public String getGa(){
        String[] splitedGav = getId().split(":");
        return splitedGav[0]+":"+splitedGav[1];
    }

    public boolean dominates(ReleaseNode other, UpdatePreferences updatePreferences) {
        return this.getNodeQuality(updatePreferences) <= other.getNodeQuality(updatePreferences) && this.changeCost <= other.changeCost && (this.getNodeQuality(updatePreferences) < other.getNodeQuality(updatePreferences) || this.changeCost < other.changeCost);
    }

    @Override
    public boolean isRelease() {
        return true;
    }

    @Override
    public boolean isLibrary() {
        return false;
    }
}
