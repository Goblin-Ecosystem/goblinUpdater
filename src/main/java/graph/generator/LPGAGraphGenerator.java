package graph.generator;

import addedvalue.AddedValueEnum;
import graph.entities.edges.JgraphtCustomEdge;
import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.NodeObject;
import graph.entities.nodes.UpdateNode;
import graph.structures.UpdateGraph;
import bazarRefonte.UpdatePreferences;
import org.json.simple.JSONObject;
import project.Project;
import util.GoblinWeaverHelpers;
import util.YmlConfReader;

import java.util.Set;

public class LPGAGraphGenerator implements GraphGenerator<UpdateNode, UpdateEdge>  {
    @Override
    public UpdateGraph<UpdateNode, UpdateEdge>  computeUpdateGraph(Project project, UpdatePreferences updatePreferences) {
        //TODO use Maven Preferences
        Set<AddedValueEnum> addedValuesToCompute = YmlConfReader.getInstance().getAddedValueEnumSet();
        JSONObject jsonDirectPossibilitiesRootedGraph = GoblinWeaverHelpers.getDirectPossibilitiesWithTransitiveRootedGraph(project.getDirectDependencies(), addedValuesToCompute);
        JgraphtGraphGenerator jgraphtGraphGenerator = new JgraphtGraphGenerator();
        UpdateGraph<UpdateNode, UpdateEdge> updateGraph = jgraphtGraphGenerator.generateRootedGraphFromJsonObject(jsonDirectPossibilitiesRootedGraph, addedValuesToCompute);
        jgraphtGraphGenerator.generateChangeEdge(project.getPath(), updateGraph);
        return updateGraph;
    }
}
