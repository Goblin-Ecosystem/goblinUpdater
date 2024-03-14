package bazarRefonte;

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
        UpdateGraphh initialGraph = graphGenerator.computeUpdateGraph(project, updatePreferences);
        Optional<Graphh> updatedGraph = solver.resolve(initialGraph, updatePreferences);
        return updatedGraph.map(ug -> projectUpdater.updateProject(project, initialGraph, ug));
    }
}
