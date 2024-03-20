package updater.lpga;

import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import graph.structures.UpdateGraph;
import updater.ProjectUpdater;
import updater.preferences.UpdatePreferences;
import project.Project;

public class LPGAProjectUpdater implements ProjectUpdater {
    @Override
    public Project updateProject(Project project, UpdateGraph<UpdateNode, UpdateEdge> initialGraph, UpdateGraph<UpdateNode, UpdateEdge> updatedGraph, UpdatePreferences updatePreferences) {
        //TODO
        return null;
    }
}
