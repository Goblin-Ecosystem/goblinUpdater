package updater.api.process.graphbased;

import java.util.Optional;

import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import updater.api.graph.UpdateGraph;
import updater.api.preferences.Preferences;
import updater.api.process.Updater;
import updater.api.project.Project;

/**
 * A refined interface for a project updater ({@link Updater}). It relies on
 * three steps to update a project: generate an update graph for the project,
 * compute an updated graph using some update solver (this may fail, hence one
 * gets an Optional of a graph), and finally update the project the updated
 * graph.
 */
public interface GraphBasedUpdater extends Updater {

    /**
     * Returns the {@link GraphGenerator} used to generate an update graph for a
     * project.
     * 
     * @return the {@link GraphGenerator} used to generate an update graph for a
     *         project.
     */
    GraphGenerator<UpdateNode, UpdateEdge> graphGenerator();

    /**
     * Returns the {@link UpdateSolver} used to compute an updated graph for a
     * project.
     * 
     * @return the {@link UpdateSolver} used to compute an updated graph for a
     *         project.
     */
    UpdateSolver solver();

    /**
     * Returns the {@link ProjectUpdater} used to update a project with an updated
     * graph.
     * 
     * @return the {@link ProjectUpdater} used to update a project with an updated
     *         graph.
     */
    ProjectUpdater projectUpdater();

    default Optional<Project> update(Project project, Preferences updatePreferences) {
        UpdateGraph<UpdateNode, UpdateEdge> initialGraph = graphGenerator().computeUpdateGraph(project,
                updatePreferences);
        Optional<UpdateGraph<UpdateNode, UpdateEdge>> updatedGraph = solver().resolve(initialGraph, updatePreferences);
        return updatedGraph.map(ug -> projectUpdater().updateProject(project, initialGraph, ug, updatePreferences));
    }

}