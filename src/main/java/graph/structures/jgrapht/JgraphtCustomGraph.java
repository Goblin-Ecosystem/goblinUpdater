package graph.structures.jgrapht;

import graph.entities.edges.AbstractEdge;
import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.ReleaseNode;
import graph.entities.nodes.UpdateNode;
import graph.structures.CustomGraph;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class JgraphtCustomGraph implements CustomGraph<UpdateNode, UpdateEdge> {
    protected final Graph<UpdateNode, UpdateEdge> graph;
    private final Map<String, UpdateNode> idToVertexMap = new HashMap<>();

    public JgraphtCustomGraph(){
        this.graph = new DefaultDirectedGraph<>(AbstractEdge.class);
    }

    public JgraphtCustomGraph(Graph<UpdateNode, UpdateEdge> graph){
        this.graph = graph;
    }

    @Override
    public void addNode(UpdateNode node) {
        idToVertexMap.put(node.id(), node);
        graph.addVertex(node);
    }

    @Override
    public void addEdgeFromNodeId(String fromId, String toId, UpdateEdge edge) {
        UpdateNode vertexFrom = idToVertexMap.get(fromId);
        UpdateNode vertexTo = idToVertexMap.get(toId);
        if(vertexFrom != null && vertexTo != null){
            graph.addEdge(vertexFrom, vertexTo, edge);
        }
    }

    @Override
    public void removeNode(UpdateNode node) {
        graph.removeVertex(node);
    }

    @Override
    public Set<UpdateNode> nodes() {
        return graph.vertexSet();
    }

    @Override
    public Set<UpdateEdge> edges() {
        return graph.edgeSet();
    }

    @Override
    public UpdateNode source(UpdateEdge edge) {
        return graph.getEdgeSource(edge);
    }

    @Override
    public UpdateNode target(UpdateEdge edge) {
        return graph.getEdgeTarget(edge);
    }

    @Override
    public Optional<UpdateNode> rootNode() {
        return graph.vertexSet().stream().filter(n -> n.equals(new ReleaseNode("ROOT"))).findFirst();
    }

    @Override
    public CustomGraph<UpdateNode, UpdateEdge> copy() {
        Graph<UpdateNode, UpdateEdge> graphCopy = new DefaultDirectedGraph<>(AbstractEdge.class);
        Graphs.addGraph(graphCopy, graph);
        return new JgraphtCustomGraph(graphCopy);
    }

    @Override
    public Set<UpdateEdge> outgoingEdgesOf(UpdateNode node) {
        return graph.outgoingEdgesOf(node);
    }

    @Override
    public String toString() {
        return "Graph size: "+graph.vertexSet().size()+" vertices, "+graph.edgeSet().size()+" edges";
    }
}
