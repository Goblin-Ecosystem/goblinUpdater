package graph.entities.nodes;

import graph.structures.Identifiable;
import java.util.Set;

import addedvalue.AddedValueEnum;

public interface UpdateNode extends Identifiable<String> {
    boolean isRelease();
    boolean isArtifact();
    Set<AddedValueEnum> knownValues();
}
