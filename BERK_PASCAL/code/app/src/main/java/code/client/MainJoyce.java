package code.client;

import java.util.Optional;

import com.google.ortools.Loader;

import code.graphs.api.Graph;
import code.graphs.impl.GraphMock001;

import static code.graphs.impl.GraphMock001.Edge001;
import static code.graphs.impl.GraphMock001.Node001;
import code.metrics.api.MetricContainer;
import code.metrics.impl.MetricMap;
import code.update.api.UpdateSolver;
import code.update.impl.or.solver.OrLPUpdateSolver;

import static code.metrics.api.Metric.FRESHNESS;
import static code.metrics.api.Metric.POPULARITY;

import java.util.Map;

public class MainJoyce {
    public static void main(String[] args) {
        Loader.loadNativeLibraries(); // ugly, specific to a kind of solver, and should be done only once -> use a better way
        MetricContainer weights = new MetricMap(Map.of(POPULARITY, 0.6, FRESHNESS, 0.4));
        Graph<Node001, Edge001> g = GraphMock001.example001();
        UpdateSolver<Node001, Edge001> solver = new OrLPUpdateSolver<>();
        Optional<Graph<Node001, Edge001>> gprime = solver.solve(g, weights);
    }    
}
