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
import updater.impl.graph.structure.edges.DependencyEdge;
import updater.impl.metrics.MetricMaxValueNormalizer;
import util.helpers.system.LoggerHelpers;
import util.helpers.system.MemoryUsageTracker;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// TODO: DRY ?
// TODO: there are frameworks/libs to compute time, use them?
// TODO: clean up by extracting methods to compute time
public class LPGAGraphGenerator implements GraphGenerator<UpdateNode, UpdateEdge>  {
    @Override
    public UpdateGraph<UpdateNode, UpdateEdge>  computeUpdateGraph(Project project, Preferences updatePreferences) {
        Set<MetricType> metricsToCompute = updatePreferences.qualityMetrics(); // TODO: OK ? We have ones to compute and others to get from the weaver. isComputed can be used.
        // 1. Generate rooted dependency graph
        long startTime = System.currentTimeMillis();
        JSONObject jsonDirectPossibilitiesRootedGraph = GoblinWeaverHelpers.getGraphFromPreferences(project.getDirectDependencies(), metricsToCompute, updatePreferences);
        MemoryUsageTracker.getInstance().checkAndUpdateMaxMemoryUsage();
        RootedGraphGenerator jgraphtGraphGenerator = new JgraphtRootedGraphGenerator();
        UpdateGraph<UpdateNode, UpdateEdge> updateGraph = jgraphtGraphGenerator.generateRootedGraphFromJsonObject(jsonDirectPossibilitiesRootedGraph, metricsToCompute);
        long endTime = System.currentTimeMillis();
        MemoryUsageTracker.getInstance().checkAndUpdateMaxMemoryUsage();
        LoggerHelpers.instance().info("Time to generate graph (ms): "+ (endTime - startTime));
        LoggerHelpers.instance().info("Release nodes size: "+updateGraph.nodes(UpdateNode::isRelease).size());
        LoggerHelpers.instance().info("Artifact nodes size: "+updateGraph.nodes(UpdateNode::isArtifact).size());
        LoggerHelpers.instance().info("Dependency edges size: "+updateGraph.edges(UpdateEdge::isDependency).size());
        LoggerHelpers.instance().info("Version edges size: "+updateGraph.edges(UpdateEdge::isVersion).size());
        // 2. Compute change edges
        startTime = System.currentTimeMillis();
        jgraphtGraphGenerator.generateChangeEdge(project.getPath(), updateGraph, updatePreferences);
        endTime = System.currentTimeMillis();
        LoggerHelpers.instance().info("Time to generate generate change edges (ms) : "+ (endTime - startTime));
        // 3. Normalize metrics
        MetricNormalizer normalizer = new MetricMaxValueNormalizer();
        startTime = System.currentTimeMillis();
        normalizer.normalize(updateGraph);
        endTime = System.currentTimeMillis();
        LoggerHelpers.instance().info("Time to normalize metrics (ms): "+ (endTime - startTime));
        // 4. Post-processing
        Optional<UpdateNode> rn = updateGraph.rootNode();
        if (rn.isPresent()) {
            Set<UpdateNode> initialGraphNodes = generateInitialGraph(updateGraph, rn.get(), new HashSet<>());
            initialGraphNodes.remove(rn.get());
            MemoryUsageTracker.getInstance().checkAndUpdateMaxMemoryUsage();
            logInitialQuality(initialGraphNodes, updatePreferences);
            MemoryUsageTracker.getInstance().checkAndUpdateMaxMemoryUsage();
        }
        // 5. Result
        return updateGraph;
    }

    private void logInitialQuality(Set<UpdateNode> initialGraphNodes, Preferences updatePreferences) {
        double totalQuality = initialGraphNodes.stream()
                .mapToDouble(n -> n.getQuality(updatePreferences))
                .sum();
        LoggerHelpers.instance().info("Initial graph quality: " + totalQuality);
    }

    private Set<UpdateNode> generateInitialGraph(UpdateGraph<UpdateNode, UpdateEdge> updateGraph, UpdateNode nodeToTreat, Set<UpdateNode> visitedNodes) {
        if(visitedNodes.contains(nodeToTreat)){
            return visitedNodes;
        }
        visitedNodes.add(nodeToTreat);
        List<DependencyEdge> depEdges = updateGraph.outgoingEdgesOf(nodeToTreat).stream().filter(UpdateEdge::isDependency).map(DependencyEdge.class::cast).toList();
        for (DependencyEdge depEdge : depEdges ){
            String version = depEdge.targetVersion();
            String artifactName = updateGraph.target(depEdge).ga();
            Optional<UpdateNode> dep = updateGraph.getNode(artifactName+":"+version);
            dep.ifPresent(updateNode -> visitedNodes.addAll(generateInitialGraph(updateGraph, updateNode, visitedNodes)));
        }
        return visitedNodes;
    }
}
