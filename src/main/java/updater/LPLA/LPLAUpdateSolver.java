package updater.LPLA;

import bazarRefonte.UpdatePreferences;
import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.ArtifactNode;
import graph.entities.nodes.ReleaseNode;
import graph.entities.nodes.UpdateNode;
import graph.structures.CustomGraph;
import graph.structures.UpdateGraph;
import updater.UpdateSolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LPLAUpdateSolver  implements UpdateSolver {

    @Override
    public <N extends UpdateNode, E extends UpdateEdge> Optional<CustomGraph> resolve(UpdateGraph<N, E> updateGraph, UpdatePreferences updatePreferences) {
        Set<UpdateNode> artifactDirectDeps = updateGraph.getRootArtifactDirectDep();
        for(UpdateNode artifactDirectDep : artifactDirectDeps){
            UpdateNode currentRelease = updateGraph.getCurrentUseReleaseOfArtifact(artifactDirectDep);
            Set<UpdateNode> allArtifactRelease = updateGraph.getAllArtifactRelease(artifactDirectDep);
            findOptimals(allArtifactRelease, currentRelease, updateGraph);
        }
        return Optional.of(updateGraph);
    }

    private static void findOptimals(Set<UpdateNode> allArtifactRelease, UpdateNode currentRelease, UpdateGraph updateGraph) {
        List<ReleaseNode> optimals = new ArrayList<>();

        for (UpdateNode candidate : allArtifactRelease) {
            ReleaseNode releaseCandidate = (ReleaseNode) candidate;
            boolean isDominant = false;
            for (ReleaseNode current : optimals) {
                if (current.dominates(releaseCandidate)) {
                    isDominant = true;
                    break;
                } else if (releaseCandidate.dominates(current)) {
                    updateGraph.removeNode(current);
                }
            }
            if (!isDominant) {
                optimals.add(releaseCandidate);
            }
        }
    }
}
