package client;

import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;
import updater.api.preferences.Preferences;
import updater.api.process.graphbased.UpdateSolver;
import static updater.api.metrics.MetricType.*;
import updater.impl.mock.graph.GraphMock;
import updater.impl.mock.preferences.PreferencesMock;
import updater.impl.process.graphbased.lpga.LPGAUpdateSolver;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import io.vavr.Tuple2;

/**
 * Basic client for LPGA project update in the Maven/Maven Central eco-system.
 * This is a mock client for testing purposes only. It may not run the whole
 * update process.
 */
public class ClientMock {

    public static void main(String[] args) {
        // inputs
        final int EXAMPLE = 2;
        UpdateGraph<UpdateNode, UpdateEdge> g = switch (EXAMPLE) {
            case 1 -> GraphMock.example001();
            case 2 -> GraphMock.example002();
            default -> throw new IllegalArgumentException("Invalid example");
        };
        List<Tuple2<String, Integer>> constrainedValues = List.of(
        // Tuple.of("e:e:1", 0),
        // Tuple.of("e:e:2", 0) //,
        // Tuple.of("f", 1)
        );
        Preferences prefs = new PreferencesMock(Map.of(
                CVE, 0.5,
                FRESHNESS, 0.3,
                POPULARITY, 0.2,
                COST, 0.6));
        // create solver and resolve update
        UpdateSolver solver = new LPGAUpdateSolver(constrainedValues);
        Optional<UpdateGraph<UpdateNode, UpdateEdge>> gprime = solver.resolve(g, prefs);
    }
}
