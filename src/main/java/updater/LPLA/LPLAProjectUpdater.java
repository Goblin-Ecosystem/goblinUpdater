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
    public Project updateProject(Project project, CustomGraph initialGraph, CustomGraph<UpdateNode, UpdateEdge> updatedGraph, UpdatePreferences updatePreferences) {
        UpdateGraph<UpdateNode, UpdateEdge> updatedGraphCasted = (UpdateGraph<UpdateNode, UpdateEdge>) updatedGraph;
        Set<UpdateNode> rootDirectArtifactDependency = updatedGraphCasted.getRootArtifactDirectDep();
        for(UpdateNode artifact : rootDirectArtifactDependency){
            System.out.println(artifact.getId());
            for(UpdateNode release : updatedGraphCasted.getAllArtifactRelease(artifact)){
                ReleaseNode releaseNode = (ReleaseNode) release;
                System.out.println("\t"+release.getId()  + " quality: "+releaseNode.getNodeQuality(updatePreferences)+" cost: "+releaseNode.getChangeCost());
            }
        }
        return null;
    }
}
