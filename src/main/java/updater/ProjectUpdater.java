package updater;

import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import graph.structures.UpdateGraph;
import project.Project;
import updater.preferences.UpdatePreferences;

public interface ProjectUpdater {
    Project updateProject(Project project, UpdateGraph<UpdateNode, UpdateEdge> initialGraph,
            UpdateGraph<UpdateNode, UpdateEdge> updatedGraph,
            UpdatePreferences updatePreferences);
}
