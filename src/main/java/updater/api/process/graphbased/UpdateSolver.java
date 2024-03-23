package updater.api.process.graphbased;

import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;
import updater.api.preferences.Preferences;

import java.util.Optional;

/**
 * Interface for solvers performing update on a update graph. Given an update
 * graph and preferences, it returns the updated graph. It may fail.
 */
public interface UpdateSolver {
    /**
     * Resolves an update graph given a set of preferences and returns the updated
     * graph. It may fail.
     */
    Optional<UpdateGraph<UpdateNode, UpdateEdge>> resolve(UpdateGraph<UpdateNode, UpdateEdge> updateGraph,
            Preferences updatePreferences);
}
