package graph.structures.jgrapht;

import graph.entities.edges.JgraphtCustomEdge;
import graph.entities.nodes.NodeObject;
import graph.entities.nodes.ReleaseNode;
import graph.structures.CustomGraph;
import graph.structures.UpdateGraph;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class JgraphtCustomGraph implements CustomGraph<NodeObject, JgraphtCustomEdge> {
    protected final Graph<NodeObject, JgraphtCustomEdge> graph;
    private final Map<String, NodeObject> idToVertexMap = new HashMap<>();

    public JgraphtCustomGraph(){
        this.graph = new DefaultDirectedGraph<>(JgraphtCustomEdge.class);
    }

    public JgraphtCustomGraph(Graph<NodeObject, JgraphtCustomEdge> graph){
        this.graph = graph;
    }

    @Override
    public void addNode(NodeObject node) {
        idToVertexMap.put(node.getId(), node);
        graph.addVertex(node);
    }

    @Override
    public void addEdgeFromNodeId(String fromId, String toId, JgraphtCustomEdge edge) {
        NodeObject vertexFrom = idToVertexMap.get(fromId);
        NodeObject vertexTo = idToVertexMap.get(toId);
        if(vertexFrom != null && vertexTo != null){
            graph.addEdge(vertexFrom, vertexTo, edge);
        }
    }

    @Override
    public void removeNode(NodeObject node) {
        graph.removeVertex(node);
    }

    @Override
    public Set<NodeObject> nodes() {
        return graph.vertexSet();
    }

    @Override
    public Set<JgraphtCustomEdge> edges() {
        return graph.edgeSet();
    }

    @Override
    public NodeObject source(JgraphtCustomEdge edge) {
        return graph.getEdgeSource(edge);
    }

    @Override
    public NodeObject target(JgraphtCustomEdge edge) {
        return graph.getEdgeTarget(edge);
    }

    @Override
    public Optional<NodeObject> rootNode() {
        return graph.vertexSet().stream().filter(n -> n.equals(new ReleaseNode("ROOT"))).findFirst();
    }

    @Override
    public CustomGraph<NodeObject, JgraphtCustomEdge> copy() {
        Graph<NodeObject, JgraphtCustomEdge> graphCopy = new DefaultDirectedGraph<>(JgraphtCustomEdge.class);
        Graphs.addGraph(graphCopy, graph);
        return new JgraphtCustomGraph(graphCopy);
    }

    @Override
    public Set<JgraphtCustomEdge> outgoingEdgesOf(NodeObject node) {
        return graph.outgoingEdgesOf(node);
    }

    @Override
    public String toString() {
        return "Graph size: "+graph.vertexSet().size()+" vertices, "+graph.edgeSet().size()+" edges";
    }
}
