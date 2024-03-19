package graph.entities.nodes;

public class ArtifactNode extends NodeObject {

    public ArtifactNode(String id) {
        super(id, NodeType.ARTIFACT);
    }

    @Override
    public boolean isRelease() {
        return false;
    }

    @Override
    public boolean isLibrary() {
        return true;
    }
}
