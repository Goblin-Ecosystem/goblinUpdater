package client;

import updater.preferences.*;
import updater.UpdateSolver;
import updater.lpga.LPGAUpdateSolver;

import java.util.Optional;

import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import graph.structures.UpdateGraph;
import graph.structures.joycegraph.GraphMock001;

/*
    LPGA (Local Possible, Global Analysis)
 */
public class ClientMock {

    public static void main(String[] args){
        UpdateGraph<UpdateNode, UpdateEdge> g = GraphMock001.example001();
        UpdateSolver solver = new LPGAUpdateSolver();
        UpdatePreferences prefs = null;
        Optional<UpdateGraph<UpdateNode, UpdateEdge>> gprime = solver.resolve(g, prefs);
    }
}
