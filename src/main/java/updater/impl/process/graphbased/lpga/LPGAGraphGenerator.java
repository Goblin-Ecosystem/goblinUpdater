package updater.impl.process.graphbased.lpga;

import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;
import updater.api.metrics.MetricType;
import updater.api.preferences.Preferences;
import updater.api.process.graphbased.GraphGenerator;
import updater.api.process.graphbased.RootedGraphGenerator;
import updater.api.project.Project;
import updater.helpers.GoblinWeaverHelpers;
import updater.impl.graph.jgrapht.JgraphtRootedGraphGenerator;

import org.json.simple.JSONObject;

import java.util.Set;

// TODO: DRY ?
public class LPGAGraphGenerator implements GraphGenerator<UpdateNode, UpdateEdge>  {
    @Override
    public UpdateGraph<UpdateNode, UpdateEdge>  computeUpdateGraph(Project project, Preferences updatePreferences) {
        Set<MetricType> metricsToCompute = updatePreferences.metrics(); // TODO: OK ? We have ones to compute and others to get from the weaver. isComputed can be used.
        JSONObject jsonDirectPossibilitiesRootedGraph = GoblinWeaverHelpers.getDirectPossibilitiesWithTransitiveRootedGraph(project.getDirectDependencies(), metricsToCompute);
        RootedGraphGenerator jgraphtGraphGenerator = new JgraphtRootedGraphGenerator();
        UpdateGraph<UpdateNode, UpdateEdge> updateGraph = jgraphtGraphGenerator.generateRootedGraphFromJsonObject(jsonDirectPossibilitiesRootedGraph, metricsToCompute);
        jgraphtGraphGenerator.generateChangeEdge(project.getPath(), updateGraph, updatePreferences);
        return updateGraph;
    }
}
