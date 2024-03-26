package updater.impl.process.graphbased.lpla;

import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;
import updater.api.metrics.MetricType;
import updater.api.preferences.Preferences;
import updater.api.process.graphbased.GraphGenerator;
import updater.api.process.graphbased.RootedGraphGenerator;
import updater.api.project.Project;
import updater.helpers.GoblinWeaverHelpers;
import updater.helpers.MaracasHelpers;
import updater.impl.graph.jgrapht.JgraphtRootedGraphGenerator;
import updater.impl.graph.structure.nodes.ReleaseNode;

import org.json.simple.JSONObject;

import util.helpers.system.LoggerHelpers;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class LPLAGraphGenerator implements GraphGenerator<UpdateNode, UpdateEdge> {
    @Override
    public UpdateGraph<UpdateNode, UpdateEdge> computeUpdateGraph(Project project,
            Preferences updatePreferences) {
        Set<MetricType> metricsToCompute = updatePreferences.qualityMetrics().stream().map(MetricType::aggregatedVersion).collect(Collectors.toSet());
        JSONObject jsonDirectPossibilitiesRootedGraph = GoblinWeaverHelpers
                .getDirectPossibilitiesRootedGraph(project.getDirectDependencies(), metricsToCompute);
        RootedGraphGenerator jgraphtGraphGenerator = new JgraphtRootedGraphGenerator();
        UpdateGraph<UpdateNode, UpdateEdge> updateGraph = jgraphtGraphGenerator
                .generateRootedGraphFromJsonObject(jsonDirectPossibilitiesRootedGraph, metricsToCompute);
        computeQualityAndCost(project, updateGraph, updatePreferences);
        return updateGraph;
    }

    private void computeQualityAndCost(Project project, UpdateGraph<UpdateNode, UpdateEdge> updateGraph,
            Preferences updatePreferences) {
        // compute direct dep cost
        LoggerHelpers.instance().info("Compute quality and cost");
        Set<UpdateNode> artifactDirectDeps = updateGraph.rootDirectDependencies();
        for (UpdateNode artifactDirectDep : artifactDirectDeps) {
            // Get current used version
            Optional<UpdateNode> optCurrentRelease = updateGraph.rootCurrentDependencyRelease(artifactDirectDep);
            if (optCurrentRelease.isPresent()) {
                ReleaseNode currentRelease = (ReleaseNode) optCurrentRelease.get();
                Set<UpdateNode> allArtifactRelease = updateGraph.versions(artifactDirectDep);
                double currentReleaseQuality = currentRelease.getNodeQuality(updatePreferences);
                for (UpdateNode artifactRelease : allArtifactRelease) {
                    // If quality of current release < artifact release, delete node
                    if (currentReleaseQuality <= ((ReleaseNode) artifactRelease).getNodeQuality(updatePreferences)
                            && !currentRelease.equals(artifactRelease)) {
                        updateGraph.removeNode(artifactRelease);
                    }
                    // Else compute change cost
                    else {
                        ((ReleaseNode) artifactRelease).setChangeCost(
                                MaracasHelpers.computeChangeCost(project.getPath(), currentRelease, artifactRelease));
                    }
                }
            }
            // TODO: clear or not clear
            // MavenLocalRepository.getInstance().clearLocalRepo();
        }
    }
}
