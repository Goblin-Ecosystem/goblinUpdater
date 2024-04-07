package updater.impl.process.graphbased.lpga;

import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;
import updater.api.metrics.MetricNormalizer;
import updater.api.metrics.MetricType;
import updater.api.preferences.Preferences;
import updater.api.process.graphbased.GraphGenerator;
import updater.api.process.graphbased.RootedGraphGenerator;
import updater.api.project.Project;
import updater.helpers.GoblinWeaverHelpers;
import updater.impl.graph.jgrapht.JgraphtRootedGraphGenerator;

import org.json.simple.JSONObject;
import updater.impl.metrics.MetricMaxValueNormalizer;
import util.helpers.system.LoggerHelpers;

import java.util.Set;

// TODO: DRY ?
public class LPGAGraphGenerator implements GraphGenerator<UpdateNode, UpdateEdge>  {
    @Override
    public UpdateGraph<UpdateNode, UpdateEdge>  computeUpdateGraph(Project project, Preferences updatePreferences) {
        Set<MetricType> metricsToCompute = updatePreferences.qualityMetrics(); // TODO: OK ? We have ones to compute and others to get from the weaver. isComputed can be used.
        long startTime = System.currentTimeMillis();
        JSONObject jsonDirectPossibilitiesRootedGraph = GoblinWeaverHelpers.getDirectNewPossibilitiesWithTransitiveRootedGraph(project.getDirectDependencies(), metricsToCompute);
        RootedGraphGenerator jgraphtGraphGenerator = new JgraphtRootedGraphGenerator();
        UpdateGraph<UpdateNode, UpdateEdge> updateGraph = jgraphtGraphGenerator.generateRootedGraphFromJsonObject(jsonDirectPossibilitiesRootedGraph, metricsToCompute);
        long endTime = System.currentTimeMillis();
        LoggerHelpers.instance().info("Time to generate graph: "+ (endTime - startTime) + " ms");
        LoggerHelpers.instance().info("Release nodes size: "+updateGraph.nodes(UpdateNode::isRelease).size());
        LoggerHelpers.instance().info("Artifact nodes size: "+updateGraph.nodes(UpdateNode::isArtifact).size());
        LoggerHelpers.instance().info("Dependency edges size: "+updateGraph.edges(UpdateEdge::isDependency).size());
        LoggerHelpers.instance().info("Version edges size: "+updateGraph.edges(UpdateEdge::isVersion).size());
        startTime = System.currentTimeMillis();
        jgraphtGraphGenerator.generateChangeEdge(project.getPath(), updateGraph, updatePreferences);
        endTime = System.currentTimeMillis();
        LoggerHelpers.instance().info("Change edge size: "+updateGraph.edges(UpdateEdge::isChange).size());
        LoggerHelpers.instance().info("Time to generate generate change edges: "+ (endTime - startTime) + " ms");
        // Normalize metrics
        MetricNormalizer normalizer = new MetricMaxValueNormalizer();
        startTime = System.currentTimeMillis();
        normalizer.normalize(updateGraph);
        endTime = System.currentTimeMillis();
        LoggerHelpers.instance().info("Time to normalize metrics: "+ (endTime - startTime) + " ms");
        return updateGraph;
    }
}
