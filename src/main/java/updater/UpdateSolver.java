package updater;

import updater.updatePreferences.*;
import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import graph.structures.CustomGraph;
import graph.structures.UpdateGraph;

import java.util.Optional;

public interface UpdateSolver {
    Optional<CustomGraph> resolve(UpdateGraph<UpdateNode, UpdateEdge> updateGraph, UpdatePreferences updatePreferences);
}
