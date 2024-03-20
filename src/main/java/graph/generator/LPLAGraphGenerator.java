package graph.generator;

import addedvalue.AddedValueEnum;
import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.ReleaseNode;
import graph.entities.nodes.UpdateNode;
import graph.structures.UpdateGraph;
import org.json.simple.JSONObject;
import project.Project;
import updater.preferences.UpdatePreferences;
import util.*;

import java.util.Set;

public class LPLAGraphGenerator implements GraphGenerator<UpdateNode, UpdateEdge> {
    @Override
    public UpdateGraph<UpdateNode, UpdateEdge> computeUpdateGraph(Project project,
            UpdatePreferences updatePreferences) {
        Set<AddedValueEnum> addedValuesToCompute = updatePreferences.getAggregatedAddedValueEnumSet();
        JSONObject jsonDirectPossibilitiesRootedGraph = GoblinWeaverHelpers
                .getDirectPossibilitiesRootedGraph(project.getDirectDependencies(), addedValuesToCompute);
        RootedGraphGenerator jgraphtGraphGenerator = new JgraphtRootedGraphGenerator();
        UpdateGraph<UpdateNode, UpdateEdge> updateGraph = jgraphtGraphGenerator
                .generateRootedGraphFromJsonObject(jsonDirectPossibilitiesRootedGraph, addedValuesToCompute);
        computeQualityAndCost(project, updateGraph, updatePreferences);
        return updateGraph;
    }

    private void computeQualityAndCost(Project project, UpdateGraph<UpdateNode, UpdateEdge> updateGraph,
            UpdatePreferences updatePreferences) {
        // compute direct dep cost
        LoggerHelpers.info("Compute quality and cost");
        Set<UpdateNode> artifactDirectDeps = updateGraph.getRootArtifactDirectDep();
        for (UpdateNode artifactDirectDep : artifactDirectDeps) {
            // Get current used version
            UpdateNode currentRelease = updateGraph.getCurrentUseReleaseOfArtifact(artifactDirectDep);
            Set<UpdateNode> allArtifactRelease = updateGraph.getAllArtifactRelease(artifactDirectDep);
            double currentReleaseQuality = ((ReleaseNode) currentRelease).getNodeQuality(updatePreferences);
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
            // TODO: clear or not clear
            // MavenLocalRepository.getInstance().clearLocalRepo();
        }
    }
}
