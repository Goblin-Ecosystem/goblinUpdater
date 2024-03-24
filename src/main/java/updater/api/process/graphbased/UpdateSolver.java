package updater.api.process.graphbased;

import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;
import updater.api.preferences.Preferences;

import java.util.Optional;

/**
 * Functional interface for solvers performing update on a update graph. Given an update
 * graph and preferences, it returns the updated graph. It may fail.
 * 
 * <P>resolve : {@link UpdateGraph} x {@link Preferences} -> Optional<{@link UpdateGraph}>
 */
@FunctionalInterface
public interface UpdateSolver {
    /**
     * Resolves an update graph given a set of preferences and returns the updated
     * graph. It may fail.
     */
    Optional<UpdateGraph<UpdateNode, UpdateEdge>> resolve(UpdateGraph<UpdateNode, UpdateEdge> updateGraph,
            Preferences updatePreferences);
}
