package graph.entities.nodes;

import graph.structures.Identifiable;
import java.util.Set;

import addedvalue.AddedValueEnum;

/**
 * Interface for nodes used in dependency update.
 */
public interface UpdateNode extends Identifiable<String> {
    /**
     * Returns true if the node represents a release (aka a version of an artifact), false otherwise
     */
    boolean isRelease();

    /**
     * Returns true if the node represents an artifact (aka a library), false otherwise
     */
    boolean isArtifact();

    /**
     * Returns set of added value kinds that can be associated to this node
     */
    Set<AddedValueEnum> knownValues();

    /**
     * Checks if the id of the edge is valid.
     */
    boolean hasValidId(String id);

    /**
     * Get the g:a part of the id of the edge. Checks if the edge has a valid id.
     */
    default String ga() {
        if (hasValidId(id())) {
            String[] parts = id().split(":");
            return parts[0] + ":" + parts[1];
        } else {
            throw new IllegalArgumentException("Invalid id for node " + id());
        }
    }
}
