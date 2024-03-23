package updater.impl.process.graphbased.lpga;

import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;
import updater.api.preferences.Preferences;
import updater.api.process.graphbased.ProjectUpdater;
import updater.api.project.Project;

public class LPGAProjectUpdater implements ProjectUpdater {
    @Override
    public Project updateProject(Project project, UpdateGraph<UpdateNode, UpdateEdge> initialGraph,
            UpdateGraph<UpdateNode, UpdateEdge> updatedGraph, Preferences updatePreferences) {
        // TODO:
        return null;
    }
}
