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

import java.util.Map;
import java.util.Optional;

/*
    LPGA (Local Possible, Global Analysis)
 */
public class ClientMock {

    public static void main(String[] args){
        UpdateGraph<UpdateNode, UpdateEdge> g = GraphMock001.example001();
        UpdateSolver solver = new LPGAUpdateSolver();
        UpdatePreferences prefs = new MockPreferences(Map.of(
            CVE, 0.6,
            FRESHNESS, 0.4
        ));
        Optional<UpdateGraph<UpdateNode, UpdateEdge>> gprime = solver.resolve(g, prefs);
    }
}
