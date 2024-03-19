package updater.LPLA;

import updater.updatePreferences.UpdatePreferences;
import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.ReleaseNode;
import graph.entities.nodes.UpdateNode;
import graph.structures.CustomGraph;
import graph.structures.UpdateGraph;
import updater.UpdateSolver;

import java.util.*;
import java.util.stream.Collectors;

public class LPLAUpdateSolver  implements UpdateSolver {

    @Override
    public Optional<CustomGraph> resolve(UpdateGraph<UpdateNode, UpdateEdge> updateGraph, UpdatePreferences updatePreferences) {
        //TODO: PAS le bon résultat (return tout)
        Set<UpdateNode> optimals = new HashSet<>();
        Set<UpdateNode> artifactDirectDeps = updateGraph.getRootArtifactDirectDep();
        for(UpdateNode artifactDirectDep : artifactDirectDeps){
            Set<UpdateNode> allArtifactRelease = updateGraph.getAllArtifactRelease(artifactDirectDep);
            optimals.addAll(findOptimals(allArtifactRelease, updatePreferences));
        }
        CustomGraph<UpdateNode, UpdateEdge> resultGraph = updateGraph.copy();
        for(UpdateNode updateNode : updateGraph.nodes()){
            if(!optimals.contains(updateNode)){
                resultGraph.removeNode(updateNode);
            }
        }
        return Optional.of(resultGraph);
    }

    private static List<UpdateNode> findOptimals(Set<UpdateNode> allArtifactRelease, UpdatePreferences updatePreferences) {
        List<UpdateNode> optimals = new ArrayList<>();

        for (ReleaseNode candidate : allArtifactRelease.stream().map(ReleaseNode.class::cast).collect(Collectors.toSet())) {
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
