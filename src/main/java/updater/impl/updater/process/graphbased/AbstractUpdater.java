package updater.impl.updater.process.graphbased;

import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import updater.api.process.graphbased.GraphBasedUpdater;
import updater.api.process.graphbased.GraphGenerator;
import updater.api.process.graphbased.ProjectUpdater;
import updater.api.process.graphbased.UpdateSolver;

/**
 * A basic implementation of the {@link GraphBasedUpdater} interface. It is
 * abstract so that users have to rely on subclasses that fix a coherent set of
 * used objects (graph generator, solver, project updater) to work with.
 */
public abstract class AbstractUpdater implements GraphBasedUpdater {
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
