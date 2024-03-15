package graph.structures;

import graph.entities.edges.PossibleEdge;
import graph.entities.edges.JgraphtCustomEdge;
import graph.entities.edges.DependencyEdge;
import graph.entities.edges.EdgeType;
import graph.entities.nodes.ArtifactNode;
import graph.entities.nodes.NodeObject;
import graph.entities.nodes.NodeType;
import graph.entities.nodes.ReleaseNode;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import util.LoggerHelpers;
import util.MaracasHelpers;

import java.util.*;
import java.util.stream.Collectors;

public class JgraphtGraphStructure implements GraphStructure{
    private final Graph<NodeObject, JgraphtCustomEdge> graph;
    private final Map<String, NodeObject> idToVertexMap = new HashMap<>();

    public JgraphtGraphStructure(){
        this.graph = new DefaultDirectedGraph<>(JgraphtCustomEdge.class);
    }

    @Override
    public Set<NodeObject> getVertexSet() {
        return graph.vertexSet();
    }

    @Override
    public Set<JgraphtCustomEdge> getEdgeSet() {
        return graph.edgeSet();
    }

    @Override
    public boolean addVertex(NodeObject vertex) {
        idToVertexMap.put(vertex.getId(), vertex);
        return graph.addVertex(vertex);
    }

    @Override
    public void addEdgeFromVertexId(String fromId, String toId, JgraphtCustomEdge customEdge) {
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
    public void generateChangeEdge(String projectPath) {
        LoggerHelpers.info("Generate change edge");
        Graph<NodeObject, JgraphtCustomEdge> graphCopy = new DefaultDirectedGraph<>(JgraphtCustomEdge.class);
        Graphs.addGraph(graphCopy, graph);
        graphCopy.vertexSet().stream()
                .filter(ReleaseNode.class::isInstance)
                .map(ReleaseNode.class::cast)
                .forEach(releaseNode -> graphCopy.outgoingEdgesOf(releaseNode).stream()
                        .filter(edge -> edge.getType().equals(EdgeType.DEPENDENCY))
                        .map(graphCopy::getEdgeTarget)
                        .forEach(artifactDependency -> graphCopy.outgoingEdgesOf(artifactDependency).stream()
                                .filter(edge -> edge.getType().equals(EdgeType.RELATIONSHIP_AR))
                                .map(graphCopy::getEdgeTarget)
                                .forEach(possibleRelease -> graph.addEdge(releaseNode, possibleRelease, new PossibleEdge()))));
        logGraphSize();
        LoggerHelpers.info("Compute change edge values");
        // compute change link quality and cost
        for(ReleaseNode sourceReleaseNode : graph.vertexSet().stream().filter(n -> n.getType().equals(NodeType.RELEASE)).map(ReleaseNode.class::cast).collect(Collectors.toSet())){
            double sourceReleaseNodeQuality = sourceReleaseNode.getNodeQuality();
            for(PossibleEdge changeEdge : getChangeEdgeOf(sourceReleaseNode)){
                ReleaseNode targetReleaseNode = (ReleaseNode) graph.getEdgeTarget(changeEdge);
                changeEdge.setQualityChange(targetReleaseNode.getNodeQuality() - sourceReleaseNodeQuality);
                // Compute cost only for direct dependencies
                if(sourceReleaseNode.getId().equals("ROOT")){
                    changeEdge.setChangeCost(MaracasHelpers.computeChangeCost(projectPath, getCurrentUseReleaseOfArtifact(new ArtifactNode(targetReleaseNode.getGa(), false)), targetReleaseNode));
                }
                else {
                    changeEdge.setChangeCost(9999999.9);
                }
            }
        }
    }


    @Override
    public ReleaseNode getCurrentUseReleaseOfArtifact(ArtifactNode artifact) {
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

    @Override
    public Set<ReleaseNode> getAllArtifactRelease(ArtifactNode artifact) {
        return graph.edgesOf(artifact).stream()
                .filter(edge -> edge.getType().equals(EdgeType.RELATIONSHIP_AR))
                .map(edge -> (ReleaseNode) graph.getEdgeTarget(edge))
                .collect(Collectors.toSet());
    }

    private Set<PossibleEdge> getChangeEdgeOf(ReleaseNode releaseNode){
        return graph.outgoingEdgesOf(releaseNode).stream().filter(PossibleEdge.class::isInstance).map(PossibleEdge.class::cast).collect(Collectors.toSet());
    }

    @Override
    public void logGraphSize(){
        LoggerHelpers.info("Graph size: "+graph.vertexSet().size()+" vertices, "+graph.edgeSet().size()+" edges");
    }
}
