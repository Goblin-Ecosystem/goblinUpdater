package oldupdater.lpga;

import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import updater.api.graph.UpdateGraph;
import updater.api.preferences.Preferences;
import updater.api.process.graphbased.ProjectUpdater;
import updater.api.project.Project;

public class LPGAProjectUpdater implements ProjectUpdater {
    @Override
    public Project updateProject(Project project, UpdateGraph<UpdateNode, UpdateEdge> initialGraph, UpdateGraph<UpdateNode, UpdateEdge> updatedGraph, Preferences updatePreferences) {
        //TODO
        return null;
    }
}
