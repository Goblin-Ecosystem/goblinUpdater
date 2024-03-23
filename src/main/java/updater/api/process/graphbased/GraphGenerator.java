package updater.api.process.graphbased;

import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;
import updater.api.preferences.Preferences;
import updater.api.project.Project;

/**
 * Interface for the generation of a graph from a project. This is the first
 * step of the {@link GraphBasedUpdater} update process.
 */
public interface GraphGenerator<N extends UpdateNode, E extends UpdateEdge> {
    /**
     * Generates a graph from a given project and preferences.
     * 
     * @param project           the project to generate the graph for
     * @param updatePreferences the preferences used in the generation of the graph
     * @return the generated graph
     */
    UpdateGraph<N, E> computeUpdateGraph(Project project, Preferences updatePreferences);
}
