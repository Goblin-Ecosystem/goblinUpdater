package graph.entities.nodes;

import graph.structures.Identifiable;
import java.util.Set;

import addedvalue.AddedValueEnum;

/**
 * Interface for nodes used in dependency update.
 */
public interface UpdateNode extends Identifiable<String> {
    /**
     * @return true if the node represents a release (aka a version of an artifact),
     *         false otherwise
     */
    boolean isRelease();

    /**
     * @return true if the node represents an artifact (aka a library), false
     *         otherwise
     */
    boolean isArtifact();

    /**
     * @return set of added value kinds that can be associated to this node
     */
    Set<AddedValueEnum> knownValues();
}
