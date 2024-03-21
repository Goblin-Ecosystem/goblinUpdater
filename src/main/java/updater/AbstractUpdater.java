package updater;

import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import graph.generator.GraphGenerator;

/**
 * A basic implementation of the {@link ThreeStepUpdater} interface. It is
 * abstract so that users have to rely on subclasses that fix a coherent set of
 * used objects (graph generator, solver, project updater) to work with.
 */
public abstract class AbstractUpdater implements ThreeStepUpdater {
    private final GraphGenerator<UpdateNode, UpdateEdge> graphGenerator;
    private final UpdateSolver solver;
    private final ProjectUpdater projectUpdater;

    protected AbstractUpdater(GraphGenerator<UpdateNode, UpdateEdge> graphGenerator, UpdateSolver solver,
            ProjectUpdater projectUpdater) {
        this.graphGenerator = graphGenerator;
        this.solver = solver;
        this.projectUpdater = projectUpdater;
    }

    @Override
    public GraphGenerator<UpdateNode, UpdateEdge> graphGenerator() {
        return graphGenerator;
    }

    @Override
    public UpdateSolver solver() {
        return solver;
    }

    @Override
    public ProjectUpdater projectUpdater() {
        return projectUpdater;
    }

}
