package graph.entities.edges;

public class DependencyEdge extends AbstractEdge {
    private final String targetVersion;
    private final String scope;

    public DependencyEdge(String id, String targetVersion, String scope) {
        super(id);
        this.targetVersion = targetVersion;
        this.scope = scope;
    }

    @Override
    public String targetVersion() {
        return targetVersion;
    }

    private String scope() {
        return scope;
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
    public boolean isChange() {
        return false;
    }
}
