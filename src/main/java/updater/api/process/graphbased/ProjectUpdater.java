package updater.api.process.graphbased;

import updater.api.graph.UpdateGraph;
import updater.api.preferences.Preferences;
import updater.api.project.Project;
import updater.impl.graph.edges.UpdateEdge;
import updater.impl.graph.nodes.UpdateNode;

/**
 * Interface for project updaters. This is the last step of the
 * {@link GraphBasedUpdater} update process.
 */
public interface ProjectUpdater {
    /**
     * Updates the given {@link Project}.
     * 
     * @param project           the {@link Project} to be updated.
     * @param initialGraph      the {@link UpdateGraph} of the original project.
     * @param updatedGraph      the {@link UpdateGraph} of the updated project.
     * @param updatePreferences the {@link Preferences} of the updater.
     * @return the updated {@link Project}.
     */
    Project updateProject(Project project, UpdateGraph<UpdateNode, UpdateEdge> initialGraph,
            UpdateGraph<UpdateNode, UpdateEdge> updatedGraph,
            Preferences updatePreferences);
}
