package updater.api.process.graphbased;

import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;
import updater.api.preferences.Preferences;
import updater.api.project.Project;

/**
 * Functional interface for project updaters. This is the last step of the
 * {@link GraphBasedUpdater} update process.
 * 
 * <P>updateProject : {@link Project} x {@link UpdateGraph} x {@link Preferences} -> {@link Project}
 */
@FunctionalInterface
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
