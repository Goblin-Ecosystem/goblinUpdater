package graph.entities.edges;

import graph.structures.Identifiable;

/**
 * Interface for edges used in dependency update.
 */
public interface UpdateEdge extends Identifiable<String> {
    /**
     * @return true if the edge represents a version (it should relate an artifact
     *         with a release),
     *         false otherwise
     */
    boolean isVersion();

    /**
     * @return true if the edge represents a dependency (it should relate a release
     *         with an artifact dependency),
     *         false otherwise
     */
    boolean isDependency();

    /**
     * @return true if the edge represents a change (it should relate a release with
     *         a release dependency),
     */
    boolean isChange();
}
