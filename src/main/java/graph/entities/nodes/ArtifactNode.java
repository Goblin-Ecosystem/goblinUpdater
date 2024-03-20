package graph.entities.nodes;

import java.util.Set;

import addedvalue.AddedValueEnum;

public class ArtifactNode extends AbstractNode {

    private static final Set<AddedValueEnum> KNOWN_VALUES = Set.of();

    public ArtifactNode(String id) {
        super(id);
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
