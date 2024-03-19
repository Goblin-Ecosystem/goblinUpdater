package updater.LPLA;

import updater.updatePreferences.UpdatePreferences;
import graph.entities.edges.UpdateEdge;
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
        //TODO: PAS le bon résultat (return tout)
        Set<UpdateNode> artifactDirectDeps = updateGraph.getRootArtifactDirectDep();
        for(UpdateNode artifactDirectDep : artifactDirectDeps){
            Set<UpdateNode> allArtifactRelease = updateGraph.getAllArtifactRelease(artifactDirectDep);
            findOptimals(allArtifactRelease, (UpdateGraph<UpdateNode, UpdateEdge>) updateGraph, updatePreferences);
            System.out.println("After find optimal: "+updateGraph.nodes().size());
        }
        return Optional.of(updateGraph);
    }

    private void findOptimals(Set<UpdateNode> allArtifactRelease, UpdateGraph<UpdateNode, UpdateEdge> updateGraph, UpdatePreferences updatePreferences) {
        List<ReleaseNode> optimals = new ArrayList<>();

        for (UpdateNode candidate : allArtifactRelease) {
            ReleaseNode releaseCandidate = (ReleaseNode) candidate;
            boolean isDominant = false;
            List<ReleaseNode> toDelete = new ArrayList<>();
            for (ReleaseNode current : optimals) {
                if (current.dominates(releaseCandidate, updatePreferences)) {
                    isDominant = true;
                    break;
                } else if (releaseCandidate.dominates(current, updatePreferences)) {
                    updateGraph.removeNode(current);
                    toDelete.add(current);
                }
            }
            optimals.removeAll(toDelete);
            if (!isDominant) {
                optimals.add(releaseCandidate);
            }
        }
        System.out.println("Optimals for change: ");
        for (ReleaseNode opti : optimals){
            System.out.println("\t"+opti.getId() + " quality:"+opti.getNodeQuality(updatePreferences)+" cost:"+opti.getChangeCost());
        }
    }
}
