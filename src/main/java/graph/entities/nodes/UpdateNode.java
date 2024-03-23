package graph.entities.nodes;

import java.util.Set;

import updater.api.metrics.MetricContainer;
import updater.api.metrics.MetricType;
import util.api.Identifiable;

/**
 * Interface for nodes used in dependency update.
 */
public interface UpdateNode extends Identifiable<String>, MetricContainer<MetricType> {
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
    Set<MetricType> knownValues();

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
