package graph.generator;

import graph.entities.edges.UpdateEdge;
import graph.structures.UpdateGraph;
import graph.entities.nodes.UpdateNode;
import bazarRefonte.UpdatePreferences;
import project.Project;

public interface GraphGenerator<N extends UpdateNode, E extends UpdateEdge> {
    UpdateGraph<N,E> computeUpdateGraph(Project project, UpdatePreferences updatePreferences);
}
