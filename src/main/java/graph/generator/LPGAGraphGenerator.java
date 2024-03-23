package graph.generator;

import addedvalue.AddedValueEnum;
import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import graph.structures.UpdateGraph;

import org.json.simple.JSONObject;
import project.Project;
import updater.preferences.UpdatePreferences;
import util.GoblinWeaverHelpers;

import java.util.Set;

// TODO: DRY ?
public class LPGAGraphGenerator implements GraphGenerator<UpdateNode, UpdateEdge>  {
    @Override
    public UpdateGraph<UpdateNode, UpdateEdge>  computeUpdateGraph(Project project, UpdatePreferences updatePreferences) {
        Set<AddedValueEnum> addedValuesToCompute = updatePreferences.metrics(); // TODO: OK ? We have ones to compute and others to get from the weaver. isComputed can be used.
        JSONObject jsonDirectPossibilitiesRootedGraph = GoblinWeaverHelpers.getDirectPossibilitiesWithTransitiveRootedGraph(project.getDirectDependencies(), addedValuesToCompute);
        RootedGraphGenerator jgraphtGraphGenerator = new JgraphtRootedGraphGenerator();
        UpdateGraph<UpdateNode, UpdateEdge> updateGraph = jgraphtGraphGenerator.generateRootedGraphFromJsonObject(jsonDirectPossibilitiesRootedGraph, addedValuesToCompute);
        jgraphtGraphGenerator.generateChangeEdge(project.getPath(), updateGraph, updatePreferences);
        return updateGraph;
    }
}
