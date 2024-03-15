package graph.generator;

import addedvalue.AddedValueEnum;
import graph.entities.edges.JgraphtCustomEdge;
import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.NodeObject;
import graph.structures.UpdateGraph;
import bazarRefonte.UpdatePreferences;
import org.json.simple.JSONObject;
import project.Project;
import util.GoblinWeaverHelpers;
import util.YmlConfReader;

import java.util.Set;

public class LPGAGraphGenerator implements GraphGenerator<NodeObject, UpdateEdge>  {
    @Override
    public UpdateGraph<NodeObject, UpdateEdge>  computeUpdateGraph(Project project, UpdatePreferences updatePreferences) {
        //TODO use Maven Preferences
        Set<AddedValueEnum> addedValuesToCompute = YmlConfReader.getInstance().getAddedValueEnumSet();
        JSONObject jsonDirectPossibilitiesRootedGraph = GoblinWeaverHelpers.getDirectPossibilitiesWithTransitiveRootedGraph(project.getDirectDependencies(), addedValuesToCompute);
        UpdateGraph<NodeObject, UpdateEdge> updateGraph = JgraphtGraphGenerator.generateRootedGraphFromJsonObject(jsonDirectPossibilitiesRootedGraph, addedValuesToCompute);
        JgraphtGraphGenerator.generateChangeEdge(project.getPath(), updateGraph);
        return updateGraph;
    }
}
