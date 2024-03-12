package graph.generator;

import addedvalue.AddedValueEnum;
import graph.entities.edges.DependencyEdge;
import graph.entities.edges.EdgeType;
import graph.entities.edges.RelationshipArEdge;
import graph.entities.nodes.ArtifactNode;
import graph.entities.nodes.NodeType;
import graph.entities.nodes.ReleaseNode;
import graph.structures.GraphStructure;
import graph.structures.JgraphtGraphStructure;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import util.LoggerHelpers;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.stream.Collectors;

public class JgraphtGraphGenerator implements GraphGenerator{

    @Override
    public GraphStructure generateRootedGraphFromJsonObject(JSONObject jsonAllPossibilitiesRootedGraph, Set<AddedValueEnum> addedValuesToCompute){
        GraphStructure graph = new JgraphtGraphStructure();
        // Add nodes
        JSONArray nodesArray = (JSONArray) jsonAllPossibilitiesRootedGraph.get("nodes");
        nodesArray.parallelStream().forEach(node -> {
            JSONObject nodeJson = (JSONObject) node;
            String id = (String) nodeJson.get("id");
            NodeType nodeType = NodeType.valueOf((String) nodeJson.get("nodeType"));
            switch (nodeType) {
                case ARTIFACT -> {
                    boolean found = (boolean) nodeJson.get("found");
                    ArtifactNode newArtifact = new ArtifactNode(id, found);
                    for(AddedValueEnum addedValueEnum : addedValuesToCompute.stream().filter(v -> v.getTargetNodeType().equals(NodeType.ARTIFACT)).collect(Collectors.toSet())){
                        try {
                            newArtifact.addAddedValue(addedValueEnum.getAddedValueClass()
                                    .getDeclaredConstructor(JSONObject.class).newInstance(nodeJson));
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            LoggerHelpers.warning(e.getMessage());
                        }
                    }
                    synchronized (graph) {
                        graph.addVertex(newArtifact);
                    }
                }
                case RELEASE -> {
                    long timestamp = (long) nodeJson.get("timestamp");
                    String version = (String) nodeJson.get("version");
                    ReleaseNode newRelease = new ReleaseNode(id, timestamp, version);
                    for(AddedValueEnum addedValueEnum : addedValuesToCompute.stream().filter(v -> v.getTargetNodeType().equals(NodeType.RELEASE)).collect(Collectors.toSet())){
                        try {
                            newRelease.addAddedValue(addedValueEnum.getAddedValueClass()
                                    .getDeclaredConstructor(JSONObject.class).newInstance(nodeJson));
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            LoggerHelpers.warning(e.getMessage());
                        }
                    }
                    synchronized (graph) {
                        graph.addVertex(newRelease);
                    }
                }
            }
        });
        // Add edges
        JSONArray edgesArray = (JSONArray) jsonAllPossibilitiesRootedGraph.get("edges");
        edgesArray.parallelStream().forEach(edge -> {
            JSONObject edgeJson = (JSONObject) edge;
            String sourceId = (String) edgeJson.get("sourceId");
            String targetId = (String) edgeJson.get("targetId");
            EdgeType edgeType = EdgeType.valueOf((String) edgeJson.get("type"));
            switch (edgeType) {
                case DEPENDENCY -> {
                    String targetVersion = (String) edgeJson.get("targetVersion");
                    String scope = (String) edgeJson.get("scope");
                    synchronized (graph) {
                        graph.addEdgeFromVertexId(sourceId, targetId, new DependencyEdge(targetVersion, scope));
                    }
                }
                case RELATIONSHIP_AR -> {
                    synchronized (graph) {
                        graph.addEdgeFromVertexId(sourceId, targetId, new RelationshipArEdge());
                    }
                }
            }
        });
        graph.logGraphSize();
        return graph;
    }
}
