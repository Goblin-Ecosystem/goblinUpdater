package graph.structures.jgrapht;

import graph.entities.edges.AbstractEdge;
import graph.entities.nodes.AbstractNode;
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

public class JgraphtCustomGraph implements CustomGraph<AbstractNode, AbstractEdge> {
    protected final Graph<AbstractNode, AbstractEdge> graph;
    private final Map<String, AbstractNode> idToVertexMap = new HashMap<>();

    public JgraphtCustomGraph(){
        this.graph = new DefaultDirectedGraph<>(AbstractEdge.class);
    }

    public JgraphtCustomGraph(Graph<AbstractNode, AbstractEdge> graph){
        this.graph = graph;
    }

    @Override
    public void addNode(AbstractNode node) {
        idToVertexMap.put(node.id(), node);
        graph.addVertex(node);
    }

    @Override
    public void addEdgeFromNodeId(String fromId, String toId, AbstractEdge edge) {
        AbstractNode vertexFrom = idToVertexMap.get(fromId);
        AbstractNode vertexTo = idToVertexMap.get(toId);
        if(vertexFrom != null && vertexTo != null){
            graph.addEdge(vertexFrom, vertexTo, edge);
        }
    }

    @Override
    public void removeNode(AbstractNode node) {
        graph.removeVertex(node);
    }

    @Override
    public Set<AbstractNode> nodes() {
        return graph.vertexSet();
    }

    @Override
    public Set<AbstractEdge> edges() {
        return graph.edgeSet();
    }

    @Override
    public AbstractNode source(AbstractEdge edge) {
        return graph.getEdgeSource(edge);
    }

    @Override
    public AbstractNode target(AbstractEdge edge) {
        return graph.getEdgeTarget(edge);
    }

    @Override
    public Optional<AbstractNode> rootNode() {
        return graph.vertexSet().stream().filter(n -> n.equals(new ReleaseNode("ROOT"))).findFirst();
    }

    @Override
    public CustomGraph<AbstractNode, AbstractEdge> copy() {
        Graph<AbstractNode, AbstractEdge> graphCopy = new DefaultDirectedGraph<>(AbstractEdge.class);
        Graphs.addGraph(graphCopy, graph);
        return new JgraphtCustomGraph(graphCopy);
    }

    @Override
    public Set<AbstractEdge> outgoingEdgesOf(AbstractNode node) {
        return graph.outgoingEdgesOf(node);
    }

    @Override
    public String toString() {
        return "Graph size: "+graph.vertexSet().size()+" vertices, "+graph.edgeSet().size()+" edges";
    }
}
