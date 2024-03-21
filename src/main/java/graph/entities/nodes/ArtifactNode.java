package graph.entities.nodes;

import java.util.Set;

import addedvalue.AddedValueEnum;

public class ArtifactNode extends AbstractNode {

    private static final Set<AddedValueEnum> KNOWN_VALUES = Set.of();

    /**
     * Constructor for artifact nodes. Assumes the id is of the form "g:a".
     * @param id the id of the node
     */
    public ArtifactNode(String id) {
        super(id);
    }

    private static final boolean isValidId(String id) {
        return id.split(":").length == 2; // FIXME: check more thoroughly
    }

    @Override
    public boolean hasValidId(String id) {
        return ArtifactNode.isValidId(id);
    }

    @Override
    public boolean isRelease() {
        return false;
    }

    @Override
    public boolean isArtifact() {
        return true;
    }

    @Override
    public Set<AddedValueEnum> knownValues() {
        return KNOWN_VALUES;
    }
}
