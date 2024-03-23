package updater.impl.updater.process.graphbased.lpga;

import updater.api.graph.UpdateGraph;
import updater.api.preferences.Preferences;
import updater.api.process.graphbased.ProjectUpdater;
import updater.api.project.Project;
import updater.impl.graph.edges.UpdateEdge;
import updater.impl.graph.nodes.UpdateNode;

public class LPGAProjectUpdater implements ProjectUpdater {
    @Override
    public Project updateProject(Project project, UpdateGraph<UpdateNode, UpdateEdge> initialGraph,
            UpdateGraph<UpdateNode, UpdateEdge> updatedGraph, Preferences updatePreferences) {
        // TODO:
        return null;
    }
}
