package client;

import updater.preferences.*;
import updater.UpdateSolver;
import updater.lpga.LPGAUpdateSolver;

import static addedvalue.AddedValueEnum.*;
import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import graph.structures.UpdateGraph;
import graph.structures.mocks.GraphMock001;
import graph.structures.mocks.MockPreferences;
import io.vavr.Tuple2;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
    LPGA (Local Possible, Global Analysis)
 */
public class ClientMock {

    public static void main(String[] args){
        // inputs
        final int EXAMPLE = 2;
        UpdateGraph<UpdateNode, UpdateEdge> g = switch (EXAMPLE) {
            case 1 -> GraphMock001.example001();
            case 2 -> GraphMock001.example002();
            default -> throw new IllegalArgumentException("Invalid example");
        };
        List<Tuple2<String, Integer>> constrainedValues = List.of(
            // Tuple.of("e:e:1", 0),
            // Tuple.of("e:e:2", 0) //,
            // Tuple.of("f", 1)
        );
        UpdatePreferences prefs = new MockPreferences(Map.of(
            CVE, 0.5,
            FRESHNESS, 0.2,
            POPULARITY, 0.3, 
            COST, 0.6
        ));
        // create solver and resolve update
        UpdateSolver solver = new LPGAUpdateSolver(constrainedValues);
        Optional<UpdateGraph<UpdateNode, UpdateEdge>> gprime = solver.resolve(g, prefs);
    }
}
