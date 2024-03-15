package graph.structures.jgrapht;

import graph.entities.edges.DependencyEdge;
import graph.entities.edges.JgraphtCustomEdge;
import graph.entities.edges.PossibleEdge;
import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.NodeObject;
import graph.entities.nodes.ReleaseNode;
import graph.entities.nodes.UpdateNode;
import graph.structures.UpdateGraph;
import util.LoggerHelpers;

import java.util.Set;
import java.util.stream.Collectors;

public class JgraphtUpdateGraph extends JgraphtCustomGraph implements UpdateGraph<NodeObject, JgraphtCustomEdge> {

    @Override
    public Set<JgraphtCustomEdge> getPossibleEdgesOf(NodeObject node) {
        return graph.outgoingEdgesOf(node).stream().filter(PossibleEdge.class::isInstance).map(PossibleEdge.class::cast).collect(Collectors.toSet());
    }

    @Override
    public NodeObject getCurrentUseReleaseOfArtifact(UpdateNode artifact) {
        DependencyEdge edge = graph.getAllEdges(new ReleaseNode("ROOT"), (NodeObject) artifact)
                .stream().filter(e -> e instanceof DependencyEdge)
                .map(e -> (DependencyEdge) e).findFirst().orElse(null);
        if(edge ==  null){
            LoggerHelpers.error("Fail to get current release of: "+artifact.getId());
            return null;
        }
        String releaseId = artifact.getId()+":"+edge.getTargetVersion();
        return graph.vertexSet().stream()
                .filter(v -> v.getId().equals(releaseId) && v instanceof ReleaseNode)
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
        return graph.edgesOf((NodeObject) artifact).stream()
                .filter(UpdateEdge::isVersion)
                .map(edge -> (ReleaseNode) graph.getEdgeTarget(edge))
                .collect(Collectors.toSet());
    }
}
