package oldupdater.lpla;

import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.ReleaseNode;
import graph.entities.nodes.UpdateNode;
import updater.api.graph.UpdateGraph;
import updater.api.preferences.Preferences;
import updater.api.process.graphbased.ProjectUpdater;
import updater.api.project.Project;

import java.util.Set;

public class LPLAProjectUpdater implements ProjectUpdater {
    @Override
    public Project updateProject(Project project, UpdateGraph<UpdateNode, UpdateEdge> initialGraph,
            UpdateGraph<UpdateNode, UpdateEdge> updatedGraph, Preferences updatePreferences) {
        UpdateGraph<UpdateNode, UpdateEdge> updatedGraphCasted = updatedGraph;
        Set<UpdateNode> rootDirectArtifactDependency = updatedGraphCasted.rootDirectDependencies();
        for (UpdateNode artifact : rootDirectArtifactDependency) {
            System.out.println(artifact.id());
            for (UpdateNode release : updatedGraphCasted.versions(artifact)) {
                ReleaseNode releaseNode = (ReleaseNode) release;
                System.out.println("\t" + release.id() + " quality: " + releaseNode.getNodeQuality(updatePreferences)
                        + " cost: " + releaseNode.getChangeCost());
            }
        }
        // TODO: Pas de print, mais un return des graphs
        return null;
    }
}
