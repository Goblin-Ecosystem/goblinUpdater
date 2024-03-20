package updater.LPLA;

import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.ReleaseNode;
import graph.entities.nodes.UpdateNode;
import graph.structures.CustomGraph;
import graph.structures.UpdateGraph;
import project.Project;
import updater.ProjectUpdater;
import updater.preferences.UpdatePreferences;

import java.util.Set;

public class LPLAProjectUpdater implements ProjectUpdater {
    @Override
    public Project updateProject(Project project, CustomGraph initialGraph, CustomGraph<UpdateNode, UpdateEdge> updatedGraph, UpdatePreferences updatePreferences) {
        UpdateGraph<UpdateNode, UpdateEdge> updatedGraphCasted = (UpdateGraph<UpdateNode, UpdateEdge>) updatedGraph;
        Set<UpdateNode> rootDirectArtifactDependency = updatedGraphCasted.getRootArtifactDirectDep();
        for(UpdateNode artifact : rootDirectArtifactDependency){
            System.out.println(artifact.id());
            for(UpdateNode release : updatedGraphCasted.getAllArtifactRelease(artifact)){
                ReleaseNode releaseNode = (ReleaseNode) release;
                System.out.println("\t"+release.id()  + " quality: "+releaseNode.getNodeQuality(updatePreferences)+" cost: "+releaseNode.getChangeCost());
            }
        }
        //TODO: Pas de print, mais un return des graphs
        return null;
    }
}
