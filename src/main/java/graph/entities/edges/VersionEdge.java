package graph.entities.edges;

public class VersionEdge extends AbstractEdge {

    public VersionEdge(String id) {
        super(id);
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
    public boolean isChange() {
        return false;
    }
}
