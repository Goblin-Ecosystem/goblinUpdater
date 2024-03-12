package graph.entities.nodes;

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

    public String getJarName(){
        return getId().replaceAll(":","_")+".jar";
    }

    public boolean dominates(ReleaseNode other) {
        return this.getNodeQuality() <= other.getNodeQuality() && this.changeCost <= other.changeCost && (this.getNodeQuality() < other.getNodeQuality() || this.changeCost < other.changeCost);
    }
}
