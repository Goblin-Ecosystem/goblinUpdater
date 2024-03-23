package updater.impl.updater.process.graphbased.lpla;

import updater.api.graph.UpdateGraph;
import updater.api.preferences.Preferences;
import updater.api.process.graphbased.UpdateSolver;
import updater.impl.graph.edges.UpdateEdge;
import updater.impl.graph.nodes.ReleaseNode;
import updater.impl.graph.nodes.UpdateNode;

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
            optimals.addAll(findOptimals(allArtifactRelease, updatePreferences));
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

    private static List<UpdateNode> findOptimals(Set<UpdateNode> allArtifactRelease, Preferences updatePreferences) {
        List<UpdateNode> optimals = new ArrayList<>();

        for (ReleaseNode candidate : allArtifactRelease.stream().map(ReleaseNode.class::cast)
                .collect(Collectors.toSet())) {
            boolean isDominant = false;
            List<ReleaseNode> toDelete = new ArrayList<>();
            for (ReleaseNode current : optimals.stream().map(ReleaseNode.class::cast).collect(Collectors.toSet())) {
                if (current.dominates(candidate, updatePreferences)) {
                    isDominant = true;
                    break;
                } else if (candidate.dominates(current, updatePreferences)) {
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
}
