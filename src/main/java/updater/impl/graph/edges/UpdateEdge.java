package updater.impl.graph.edges;

import updater.api.metrics.MetricContainer;
import updater.api.metrics.MetricType;
import util.api.Identifiable;

/**
 * Interface for edges used in dependency update.
 */
public interface UpdateEdge extends Identifiable<String>, MetricContainer<MetricType> {
    /**
     * Returns true if the edge represents a version (it should relate an artifact with a release), false otherwise
     */
    boolean isVersion();

    /**
     * Returns true if the edge represents a dependency (it should relate a release with an artifact dependency), false otherwise
     */
    boolean isDependency();

    /**
     * Returns true if the edge represents a change (it should relate a release with a release dependency),
     */
    boolean isChange();

    /**
     * Returns the target version of the edge if it is a dependency, null otherwise. The isDependency method can be used to check beforehand if the edge represents a dependency or not.
     */
    String targetVersion();
}
