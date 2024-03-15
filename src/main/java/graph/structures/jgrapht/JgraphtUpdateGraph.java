package graph.structures.jgrapht;

import graph.entities.edges.DependencyEdge;
import graph.entities.edges.JgraphtCustomEdge;
import graph.entities.edges.PossibleEdge;
import graph.entities.nodes.NodeObject;
import graph.entities.nodes.ReleaseNode;
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
    public NodeObject getCurrentUseReleaseOfArtifact(NodeObject artifact) {
        DependencyEdge edge = graph.getAllEdges(new ReleaseNode("ROOT"), artifact)
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
}
