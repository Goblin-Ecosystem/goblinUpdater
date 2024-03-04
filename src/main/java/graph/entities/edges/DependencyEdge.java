package graph.entities.edges;

public class DependencyEdge extends CustomEdge {
    private final String targetVersion;
    private final String scope;

    public DependencyEdge(String targetVersion, String scope) {
        super(EdgeType.DEPENDENCY);
        this.targetVersion = targetVersion;
        this.scope = scope;
    }

    public String getTargetVersion() {
        return targetVersion;
    }
}
