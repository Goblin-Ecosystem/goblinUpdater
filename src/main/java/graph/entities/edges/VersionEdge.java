package graph.entities.edges;

public class VersionEdge extends JgraphtCustomEdge {

    public VersionEdge() {
        super(EdgeType.RELATIONSHIP_AR);
    }

    @Override
    public boolean isVersion() {
        return true;
    }

    @Override
    public boolean isDependency() {
        return false;
    }

    @Override
    public boolean isPossible() {
        return false;
    }
}
