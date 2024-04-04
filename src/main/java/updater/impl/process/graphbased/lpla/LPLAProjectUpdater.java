package updater.impl.process.graphbased.lpla;

import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;
import updater.api.preferences.Preferences;
import updater.api.process.graphbased.ProjectUpdater;
import updater.api.project.Project;
import updater.impl.graph.structure.edges.ChangeEdge;
import updater.impl.graph.structure.nodes.ReleaseNode;

import java.util.Set;

public class LPLAProjectUpdater implements ProjectUpdater {
    @Override
    public Project updateProject(Project project, UpdateGraph<UpdateNode, UpdateEdge> initialGraph,
            UpdateGraph<UpdateNode, UpdateEdge> updatedGraph, Preferences updatePreferences) {
        Set<UpdateNode> rootDirectArtifactDependency = updatedGraph.rootDirectDependencies();
        for (UpdateNode artifact : rootDirectArtifactDependency) {
            System.out.println(artifact.id());
            for (UpdateNode release : updatedGraph.versions(artifact)) {
                ReleaseNode releaseNode = (ReleaseNode) release;
                double releaseNodeCost = updatedGraph.incomingEdgesOf(releaseNode).stream().filter(UpdateEdge::isChange).map(ChangeEdge.class::cast).findFirst().get().cost();
                System.out.println("\t" + release.id() + " quality: " + releaseNode.getQuality(updatePreferences)
                        + " cost: " + releaseNodeCost);
            }
        }
        // TODO: Pas de print, mais un return des graphs
        return null;
    }
}
