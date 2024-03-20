package graph.structures.jgrapht;

import graph.entities.edges.DependencyEdge;
import graph.entities.edges.ChangeEdge;
import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.AbstractNode;
import graph.entities.nodes.ReleaseNode;
import graph.entities.nodes.UpdateNode;
import graph.structures.UpdateGraph;
import util.LoggerHelpers;

import java.util.Set;
import java.util.stream.Collectors;

public class JgraphtUpdateGraph extends JgraphtCustomGraph implements UpdateGraph<UpdateNode, UpdateEdge> {

    @Override
    public Set<UpdateEdge> getPossibleEdgesOf(UpdateNode node) {
        return graph.outgoingEdgesOf(node).stream().filter(ChangeEdge.class::isInstance).map(ChangeEdge.class::cast).collect(Collectors.toSet());
    }

    @Override
    public AbstractNode getCurrentUseReleaseOfArtifact(UpdateNode artifact) {
        DependencyEdge edge = graph.getAllEdges(new ReleaseNode("ROOT"), artifact)
                .stream().filter(DependencyEdge.class::isInstance)
                .map(e -> (DependencyEdge) e).findFirst().orElse(null);
        if(edge ==  null){
            LoggerHelpers.error("Fail to get current release of: "+artifact.id());
            return null;
        }
        String releaseId = artifact.id()+":"+edge.targetVersion();
        return graph.vertexSet().stream()
                .filter(v -> v.id().equals(releaseId) && v instanceof ReleaseNode)
                .map(v -> (ReleaseNode) v)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Set<UpdateNode> getRootArtifactDirectDep() {
        ReleaseNode root = new ReleaseNode("ROOT");
        return graph.edgesOf(root).stream()
                .filter(edge -> edge.isDependency() && graph.getEdgeSource(edge).equals(root))
                .map(graph::getEdgeTarget)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<UpdateNode> getAllArtifactRelease(UpdateNode artifact) {
        return graph.edgesOf(artifact).stream()
                .filter(UpdateEdge::isVersion)
                .map(edge -> (ReleaseNode) graph.getEdgeTarget(edge))
                .collect(Collectors.toSet());
    }
}
