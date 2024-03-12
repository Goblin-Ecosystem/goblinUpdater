package graph.structures;

import graph.entities.edges.ChangeEdge;
import graph.entities.edges.CustomEdge;
import graph.entities.nodes.ArtifactNode;
import graph.entities.nodes.NodeObject;
import graph.entities.nodes.ReleaseNode;

import java.util.Set;

public interface GraphStructure {
    Set<NodeObject> getVertexSet();
    Set<CustomEdge> getEdgeSet();
    boolean addVertex(NodeObject vertex);
    void addEdgeFromVertexId(String fromId, String toId, CustomEdge customEdge);
    void removeVertex(NodeObject vertex);
    Set<ArtifactNode> getRootArtifactDirectDep();
    ReleaseNode getCurrentUseReleaseOfArtifact(ArtifactNode artifact);
    Set<ReleaseNode> getAllArtifactRelease(ArtifactNode artifact);
    void generateChangeEdge();
    Set<ChangeEdge> getChangeEdgeOf(ReleaseNode releaseNode);
    NodeObject getEdgeTarget(CustomEdge edge);
}
