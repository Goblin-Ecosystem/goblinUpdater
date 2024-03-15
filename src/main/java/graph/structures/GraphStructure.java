package graph.structures;

import graph.entities.edges.JgraphtCustomEdge;
import graph.entities.nodes.ArtifactNode;
import graph.entities.nodes.NodeObject;
import graph.entities.nodes.ReleaseNode;

import java.util.Set;

public interface GraphStructure {
    Set<NodeObject> getVertexSet();
    Set<JgraphtCustomEdge> getEdgeSet();
    boolean addVertex(NodeObject vertex);
    void addEdgeFromVertexId(String fromId, String toId, JgraphtCustomEdge customEdge);
    void removeVertex(NodeObject vertex);
    Set<ArtifactNode> getRootArtifactDirectDep();
    ReleaseNode getCurrentUseReleaseOfArtifact(ArtifactNode artifact);
    Set<ReleaseNode> getAllArtifactRelease(ArtifactNode artifact);
    void generateChangeEdge(String projectPath);
    void logGraphSize();
}
