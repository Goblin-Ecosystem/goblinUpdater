package updater.impl.process.graphbased.lpla;

import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;
import updater.api.preferences.Preferences;
import updater.api.process.graphbased.UpdateSolver;
import updater.impl.graph.structure.edges.ChangeEdge;
import updater.impl.graph.structure.nodes.ReleaseNode;

import java.util.*;
import java.util.stream.Collectors;

public class LPLAUpdateSolver implements UpdateSolver {

    @Override
    public Optional<UpdateGraph<UpdateNode, UpdateEdge>> resolve(UpdateGraph<UpdateNode, UpdateEdge> updateGraph,
            Preferences updatePreferences) {
        Set<UpdateNode> optimals = new HashSet<>();
        Set<UpdateNode> artifactDirectDeps = updateGraph.rootDirectDependencies();
        for (UpdateNode artifactDirectDep : artifactDirectDeps) {
            Set<UpdateNode> allArtifactRelease = updateGraph.versions(artifactDirectDep);
            optimals.addAll(findOptimals(allArtifactRelease, updateGraph, updatePreferences));
        }
        // FIXME: is it working with a reference copy only?
        UpdateGraph<UpdateNode, UpdateEdge> copyGraph = updateGraph.copy();
        for (UpdateNode updateNode : copyGraph.nodes()) {
            if (updateNode.isRelease() && !updateNode.id().equals("ROOT") && !optimals.contains(updateNode)) {
                updateGraph.removeNode(updateNode);
            }
        }
        return Optional.of(updateGraph);
    }

    private static List<UpdateNode> findOptimals(Set<UpdateNode> allArtifactRelease, UpdateGraph<UpdateNode, UpdateEdge> updateGraph, Preferences updatePreferences) {
        List<UpdateNode> optimals = new ArrayList<>();

        for (ReleaseNode candidate : allArtifactRelease.stream().map(ReleaseNode.class::cast)
                .collect(Collectors.toSet())) {
            boolean isDominant = false;
            List<ReleaseNode> toDelete = new ArrayList<>();
            for (ReleaseNode current : optimals.stream().map(ReleaseNode.class::cast).collect(Collectors.toSet())) {
                if (dominates(current, candidate, updateGraph, updatePreferences)) {
                    isDominant = true;
                    break;
                } else if (dominates(candidate, current, updateGraph, updatePreferences)) {
                    toDelete.add(current);
                }
            }
            optimals.removeAll(toDelete);

            if (!isDominant) {
                optimals.add(candidate);
            }
        }
        return optimals;
    }

    private static boolean dominates(ReleaseNode sourceRelease, ReleaseNode comparedRelease, UpdateGraph<UpdateNode, UpdateEdge> updateGraph, Preferences updatePreferences) {
        double sourceReleaseQuality = sourceRelease.getQuality(updatePreferences);
        double comparedReleaseQuality = comparedRelease.getQuality(updatePreferences);
        double sourceReleaseCost = updateGraph.incomingEdgesOf(sourceRelease).stream().filter(UpdateEdge::isChange).map(ChangeEdge.class::cast).findFirst().get().cost();
        double comparedReleaseCost = updateGraph.incomingEdgesOf(sourceRelease).stream().filter(UpdateEdge::isChange).map(ChangeEdge.class::cast).findFirst().get().cost();
        return sourceReleaseQuality <= comparedReleaseQuality
                && sourceReleaseCost <= comparedReleaseCost
                && (sourceReleaseQuality < comparedReleaseQuality
                || sourceReleaseCost < comparedReleaseCost);
    }
}
