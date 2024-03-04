package graph.entities.nodes;

public class ArtifactNode extends NodeObject {
    private final boolean found;

    public ArtifactNode(String id, boolean found) {
        super(id, NodeType.ARTIFACT);
        this.found = found;
    }
}
