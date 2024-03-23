package updater.impl.graph.jgrapht;

import util.api.CustomGraph;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;

import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// FIXME: should use N extends Identifiable/E instead of UpdateNode/UpdateEdge
public class JgraphtCustomGraph implements CustomGraph<UpdateNode, UpdateEdge> {
    protected final Graph<UpdateNode, UpdateEdge> graph; // FIXME: why not private?
    private final Map<String, UpdateNode> idToVertexMap = new HashMap<>();

    public JgraphtCustomGraph(){
        this.graph = new DefaultDirectedGraph<>(UpdateEdge.class);
    }

    public JgraphtCustomGraph(Graph<UpdateNode, UpdateEdge> graph){
        this.graph = graph; // FIXME: sure? different of copy.
    }

    @Override
    public void addNode(UpdateNode node) {
        if (graph.addVertex(node)) {
            idToVertexMap.put(node.id(), node);
        }
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
        if (graph.removeVertex(node)) {
            idToVertexMap.remove(node.id());
        }
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

    /**
     * Returns the root of the graph is there is one.
     * Redefines the version in CustomGraph to match only with a ReleaseNode.
     */
    @Override
    public Optional<UpdateNode> rootNode() {
        return nodes(UpdateNode::isRelease).stream().filter(n -> n.id().equals(ROOT_ID)).findFirst();
    }

    @Override
    public CustomGraph<UpdateNode, UpdateEdge> copy() {
        Graph<UpdateNode, UpdateEdge> graphCopy = new DefaultDirectedGraph<>(UpdateEdge.class);
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
