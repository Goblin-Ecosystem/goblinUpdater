package bazarRefonte;

import java.util.Optional;

public interface UpdateSolver {
    <N extends UpdateNode, E extends UpdateEdge> Optional<Graphh> resolve(UpdateGraphh<N, E> updateGraph, UpdatePreferences updatePreferences);
}
