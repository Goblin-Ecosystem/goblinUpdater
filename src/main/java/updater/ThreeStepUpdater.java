package updater;

import java.util.Optional;

import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import graph.generator.GraphGenerator;
import graph.structures.UpdateGraph;
import project.Project;
import updater.preferences.UpdatePreferences;

/**
 * A refined interface for a project updater ({@link Updater}). It relies on three steps to update a project: generate an update graph for the project, compute an updated graph using some update solver (this may fail, hence one gets an Optional of a graph), and finally update the project the updated graph.
 */
public interface ThreeStepUpdater extends Updater {

    GraphGenerator<UpdateNode, UpdateEdge> graphGenerator();
    UpdateSolver solver();
    ProjectUpdater projectUpdater();

    default Optional<Project> update(Project project, UpdatePreferences updatePreferences) {
        UpdateGraph<UpdateNode, UpdateEdge> initialGraph = graphGenerator().computeUpdateGraph(project,
                updatePreferences);
        Optional<UpdateGraph<UpdateNode, UpdateEdge>> updatedGraph = solver().resolve(initialGraph, updatePreferences);
        return updatedGraph.map(ug -> projectUpdater().updateProject(project, initialGraph, ug, updatePreferences));
    }


}