package updater;

import bazarRefonte.*;
import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import graph.structures.CustomGraph;
import graph.structures.UpdateGraph;

import java.util.Optional;

public interface UpdateSolver {
    <N extends UpdateNode, E extends UpdateEdge> Optional<CustomGraph> resolve(UpdateGraph<N, E> updateGraph, UpdatePreferences updatePreferences);
}
