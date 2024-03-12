package code.update.api;

import java.util.Optional;

import code.graphs.api.Graph;
import code.metrics.api.MetricContainer;

public interface LPUpdateSolver<N extends MetricContainer & UpdateNode, E extends MetricContainer & UpdateEdge, T, S>
                extends UpdateSolver<N, E> {
        T createProblem(Graph<N, E> graph, MetricContainer weights);

        Optional<S> solveProblem(T problem, Graph<N, E> graph, MetricContainer weights);

        Optional<Graph<N, E>> solutionToGraph(S solution,
                        Graph<N, E> graph);

        default Optional<Graph<N, E>> solve(
                        Graph<N, E> graph,
                        MetricContainer weights) {
                T problem = createProblem(graph, weights);
                return solveProblem(problem, graph, weights)
                                .flatMap(s -> solutionToGraph(s, graph));
        }
}
