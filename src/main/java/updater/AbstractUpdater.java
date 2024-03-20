package updater;

import graph.structures.CustomGraph;
import graph.generator.GraphGenerator;
import graph.structures.UpdateGraph;
import project.Project;
import updater.preferences.*;

import java.util.Optional;

public class AbstractUpdater implements Updater {
    private final GraphGenerator graphGenerator;
    private final UpdateSolver solver;
    private final ProjectUpdater projectUpdater;

    protected AbstractUpdater(GraphGenerator graphGenerator, UpdateSolver solver, ProjectUpdater projectUpdater) {
        this.graphGenerator = graphGenerator;
        this.solver = solver;
        this.projectUpdater = projectUpdater;
    }

    @Override
    public Optional<Project> update(Project project, UpdatePreferences updatePreferences) {
        UpdateGraph initialGraph = graphGenerator.computeUpdateGraph(project, updatePreferences);
        Optional<CustomGraph> updatedGraph = solver.resolve(initialGraph, updatePreferences);
        return updatedGraph.map(ug -> projectUpdater.updateProject(project, initialGraph, ug, updatePreferences));
    }
}
