package updater.api.process.graphbased;

import java.util.Optional;

import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;
import updater.api.preferences.Preferences;
import updater.api.process.Updater;
import updater.api.project.Project;
import util.helpers.system.LoggerHelpers;

/**
 * A refined interface for a project updater ({@link Updater}). It relies on
 * three steps to update a project: generate an update graph for the project (using a {@link GraphGenerator}),
 * compute an updated graph using some {@link UpdateSolver} (this may fail, hence one
 * gets an Optional of a graph), and finally update the project using the updated
 * graph (using a {@link ProjectUpdater}).
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

    /**
     * Default implementation of the {@link #update(Project, Preferences)} method.
     */
    default Optional<Project> update(Project project, Preferences updatePreferences) {
        UpdateGraph<UpdateNode, UpdateEdge> initialGraph = graphGenerator().computeUpdateGraph(project,
                updatePreferences);
        long startTime = System.currentTimeMillis();
        Optional<UpdateGraph<UpdateNode, UpdateEdge>> updatedGraph = solver().resolve(initialGraph, updatePreferences);
        long endTime = System.currentTimeMillis();
        LoggerHelpers.instance().info("Time to solve: "+ (endTime - startTime) + " ms");
        return updatedGraph.map(ug -> projectUpdater().updateProject(project, initialGraph, ug, updatePreferences));
    }

}