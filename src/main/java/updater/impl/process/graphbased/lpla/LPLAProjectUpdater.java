package updater.impl.process.graphbased.lpla;

import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;
import updater.api.preferences.Preferences;
import updater.api.process.graphbased.ProjectUpdater;
import updater.api.project.Project;
import updater.impl.graph.structure.nodes.ReleaseNode;

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
