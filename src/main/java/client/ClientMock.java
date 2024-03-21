package client;

import updater.preferences.*;
import updater.UpdateSolver;
import updater.lpga.LPGAUpdateSolver;

import static addedvalue.AddedValueEnum.CVE;
import static addedvalue.AddedValueEnum.FRESHNESS;
import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import graph.structures.UpdateGraph;
import graph.structures.mocks.GraphMock001;
import graph.structures.mocks.MockPreferences;
import io.vavr.Tuple;
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
        // UpdateGraph<UpdateNode, UpdateEdge> g = GraphMock001.example001();
        UpdateGraph<UpdateNode, UpdateEdge> g = GraphMock001.example002();
        List<Tuple2<String, Integer>> constrainedValues = List.of(
            // Tuple.of("j", 1),
            // Tuple.of("d", 1),
            // Tuple.of("f", 1)
        );
        UpdatePreferences prefs = new MockPreferences(Map.of(
            CVE, 0.6,
            FRESHNESS, 0.4
        ));
        // create solver and resolve update
        UpdateSolver solver = new LPGAUpdateSolver(constrainedValues);
        Optional<UpdateGraph<UpdateNode, UpdateEdge>> gprime = solver.resolve(g, prefs);
    }
}
