package updater;

import updater.updatePreferences.UpdatePreferences;
import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import graph.structures.CustomGraph;
import graph.structures.UpdateGraph;
import project.Project;

public interface ProjectUpdater {
    Project updateProject(Project project, CustomGraph initialGraph, UpdateGraph<UpdateNode, UpdateEdge> updatedGraph, UpdatePreferences updatePreferences);
}
