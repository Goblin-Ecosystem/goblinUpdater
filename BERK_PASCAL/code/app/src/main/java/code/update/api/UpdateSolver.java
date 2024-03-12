package code.update.api;

import java.util.Optional;

import code.graphs.api.Graph;
import code.metrics.api.MetricContainer;

public interface UpdateSolver<N extends MetricContainer & UpdateNode, E extends MetricContainer & UpdateEdge> {
    Optional<Graph<N, E>> solve(Graph<N, E> graph, MetricContainer weights);
}
