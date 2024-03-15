package updater.LPLA;

import updater.updatePreferences.UpdatePreferences;
import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.ReleaseNode;
import graph.entities.nodes.UpdateNode;
import graph.structures.CustomGraph;
import graph.structures.UpdateGraph;
import project.Project;
import updater.ProjectUpdater;

import java.util.Set;

public class LPLAProjectUpdater implements ProjectUpdater {
    @Override
    public Project updateProject(Project project, CustomGraph initialGraph, UpdateGraph<UpdateNode, UpdateEdge> updatedGraph, UpdatePreferences updatePreferences) {
        Set<UpdateNode> rootDirectArtifactDependency = updatedGraph.getRootArtifactDirectDep();
        for(UpdateNode artifact : rootDirectArtifactDependency){
            System.out.println(artifact.getId());
            for(UpdateNode release : updatedGraph.getAllArtifactRelease(artifact)){
                ReleaseNode releaseNode = (ReleaseNode) release;
                System.out.println("\t"+release.getId()  + " quality:"+releaseNode.getNodeQuality(updatePreferences)+" cost:"+releaseNode.getChangeCost());
            }
        }
        return null;
    }
}
