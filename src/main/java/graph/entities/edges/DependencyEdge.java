package graph.entities.edges;

public class DependencyEdge extends JgraphtCustomEdge {
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

    @Override
    public boolean isVersion() {
        return false;
    }

    @Override
    public boolean isDependency() {
        return true;
    }

    @Override
    public boolean isPossible() {
        return false;
    }
}
