package updater;

import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import graph.structures.CustomGraph;
import project.Project;
import updater.preferences.UpdatePreferences;

public interface ProjectUpdater {
    Project updateProject(Project project, CustomGraph initialGraph, CustomGraph<UpdateNode, UpdateEdge> updatedGraph, UpdatePreferences updatePreferences);
}
