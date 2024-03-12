package graph.structures;

import graph.entities.edges.ChangeEdge;
import graph.entities.edges.CustomEdge;
import graph.entities.edges.DependencyEdge;
import graph.entities.edges.EdgeType;
import graph.entities.nodes.ArtifactNode;
import graph.entities.nodes.NodeObject;
import graph.entities.nodes.ReleaseNode;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.w3c.dom.Node;

import java.util.*;
import java.util.stream.Collectors;

public class JgraphtGraphStructure implements GraphStructure{
    private final Graph<NodeObject, CustomEdge> graph;
    private Map<String, NodeObject> idToVertexMap = new HashMap<>();

    public JgraphtGraphStructure(){
        this.graph = new DefaultDirectedGraph<>(CustomEdge.class);
    }

    @Override
    public Set<NodeObject> getVertexSet() {
        return graph.vertexSet();
    }

    @Override
    public Set<CustomEdge> getEdgeSet() {
        return graph.edgeSet();
    }

    @Override
    public boolean addVertex(NodeObject vertex) {
        idToVertexMap.put(vertex.getId(), vertex);
        return graph.addVertex(vertex);
    }

    @Override
    public void addEdgeFromVertexId(String fromId, String toId, CustomEdge customEdge) {
        NodeObject vertexFrom = idToVertexMap.get(fromId);
        NodeObject vertexTo = idToVertexMap.get(toId);
        if(vertexFrom != null && vertexTo != null){
            graph.addEdge(vertexFrom, vertexTo, customEdge);
        }
    }

    @Override
    public void removeVertex(NodeObject vertex) {
        graph.removeVertex(vertex);
    }

    @Override
    public Set<ArtifactNode> getRootArtifactDirectDep() {
        ReleaseNode root = new ReleaseNode("ROOT");
        return graph.edgesOf(root).stream()
                .filter(edge -> edge.getType().equals(EdgeType.DEPENDENCY) && graph.getEdgeSource(edge).equals(root))
                .map(edge -> (ArtifactNode) graph.getEdgeTarget(edge))
                .collect(Collectors.toSet());
    }

    @Override
    public void generateChangeEdge() {
        Graph<NodeObject, CustomEdge> graphCopy = new DefaultDirectedGraph<>(CustomEdge.class);
        Graphs.addGraph(graphCopy, graph);
        graphCopy.vertexSet().stream()
                .filter(ReleaseNode.class::isInstance)
                .map(ReleaseNode.class::cast)
                .forEach(releaseNode -> {
                    graphCopy.outgoingEdgesOf(releaseNode).stream()
                            .filter(edge -> edge.getType().equals(EdgeType.DEPENDENCY))
                            .map(graphCopy::getEdgeTarget)
                            .forEach(artifactDependency -> graphCopy.outgoingEdgesOf(artifactDependency).stream()
                                    .filter(edge -> edge.getType().equals(EdgeType.RELATIONSHIP_AR))
                                    .map(graphCopy::getEdgeTarget)
                                    .forEach(possibleRelease -> graph.addEdge(releaseNode, possibleRelease, new ChangeEdge())));
                });
    }


    @Override
    public ReleaseNode getCurrentUseReleaseOfArtifact(ArtifactNode artifact) {
        DependencyEdge edge = graph.getAllEdges(new ReleaseNode("ROOT"), artifact)
                .stream().filter(e -> e instanceof DependencyEdge)
                .map(e -> (DependencyEdge) e).findFirst().orElse(null);
        if(edge ==  null){
            System.out.println("Fail to get current release of: "+artifact.getId());
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
    public Set<ReleaseNode> getAllArtifactRelease(ArtifactNode artifact) {
        return graph.edgesOf(artifact).stream()
                .filter(edge -> edge.getType().equals(EdgeType.RELATIONSHIP_AR))
                .map(edge -> (ReleaseNode) graph.getEdgeTarget(edge))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ChangeEdge> getChangeEdgeOf(ReleaseNode releaseNode){
        return graph.outgoingEdgesOf(releaseNode).stream().filter(ChangeEdge.class::isInstance).map(ChangeEdge.class::cast).collect(Collectors.toSet());
    }

    @Override
    public NodeObject getEdgeTarget(CustomEdge edge){
        return graph.getEdgeTarget(edge);
    }
}
