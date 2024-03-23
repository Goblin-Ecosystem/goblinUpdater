package graph.generator;

import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import updater.api.graph.UpdateGraph;
import updater.api.metrics.MetricType;
import updater.api.preferences.Preferences;
import updater.api.process.graphbased.GraphGenerator;
import updater.api.project.Project;
import util.helpers.GoblinWeaverHelpers;

import org.json.simple.JSONObject;

import java.util.Set;

// TODO: DRY ?
public class LPGAGraphGenerator implements GraphGenerator<UpdateNode, UpdateEdge>  {
    @Override
    public UpdateGraph<UpdateNode, UpdateEdge>  computeUpdateGraph(Project project, Preferences updatePreferences) {
        Set<MetricType> addedValuesToCompute = updatePreferences.metrics(); // TODO: OK ? We have ones to compute and others to get from the weaver. isComputed can be used.
        JSONObject jsonDirectPossibilitiesRootedGraph = GoblinWeaverHelpers.getDirectPossibilitiesWithTransitiveRootedGraph(project.getDirectDependencies(), addedValuesToCompute);
        RootedGraphGenerator jgraphtGraphGenerator = new JgraphtRootedGraphGenerator();
        UpdateGraph<UpdateNode, UpdateEdge> updateGraph = jgraphtGraphGenerator.generateRootedGraphFromJsonObject(jsonDirectPossibilitiesRootedGraph, addedValuesToCompute);
        jgraphtGraphGenerator.generateChangeEdge(project.getPath(), updateGraph, updatePreferences);
        return updateGraph;
    }
}
