package graph.structures;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Base interface for graphs used in dependency update. Shall be fused into an interface global to all of the Goblin framework in some future (or not ...). Hence, rather use UpdateGraph for update matters.
 * @param <N> type of the nodes in the graph (should implement the Identifiable interface)
 * @param <E> type of the edges in the graph
 */
public interface CustomGraph<N extends Identifiable<String>, E> {
    /**
     * Adds a node to the graph. If the node already exists, it is ignored.
     * @param node the node to add
     */
    void addNode(N node);

    /**
     * Adds an edge from one node to another (nodes given with their ids). If either node does not, or if the edge already exists, it is ignored.
     * @param fromId the id of the source node
     * @param toId the id of the target node
     * @param edge the edge to add
     */
    // FIXME: it may seem strange to give an edge to add an edge, to be explained or masked in some way.
    void addEdgeFromNodeId(String fromId, String toId, E edge);

    /**
     * Removes a node from the graph. If the node does not exist, it is ignored. Also removes all edges connected to this node.
     * @param node the node to remove
     */
    void removeNode(N node);

    /**
     * Returns the set of all nodes in the graph.
     */
    Set<N> nodes();

    /**
     * Returns the set of all edges in the graph.
     */
    Set<E> edges();

    /**
     * Returns the set of all nodes in the graph that satisfy the given predicate. Default implementation filters the set of all nodes, can be specialized in classes.
     * @param p the predicate to apply
     */
    default Set<N> nodes(Predicate<N> p) {
        return nodes().stream().filter(p).collect(Collectors.toSet());
    }

    /**
     * Returns the set of all edges in the graph that satisfy the given predicate. Default implementation filters the set of all edges, can be specialized in classes.
     * @param p the predicate to apply
     */
    default Set<E> edges(Predicate<E> p) {
        return edges().stream().filter(p).collect(Collectors.toSet());
    }

    /**
     * Gives the source node of an edge.
     * @param edge the edge to get the source of
     */
    N source(E edge);

    /**
     * Gives the target node of an edge.
     * @param edge the edge to get the target of
     */
    N target(E edge);

    /**
     * Default id used for the root node in the graph if there is one.
     */
    static final String ROOT_ID = "ROOT";

    /**
     * Returns the root of the graph is there is one. Default implementation uses the ROOT_ID constant and searches for the first node to have this as its id. Can be specialized in an implementation class.
     */
    default Optional<N> rootNode() {
        return nodes().stream().filter(n -> n.id().equals(ROOT_ID)).findFirst();
    }

    /**
     * Returns the set of all outgoing edges from a given node.
     * @param node the node to get the outgoing edges for
     */
    Set<E> outgoingEdgesOf(N node);

    /**
     * Makes a copy of the graph.
     */
    // FIXME: this is not really needed since we can just use the copy constructor in Graphs (if there is always one), or follow the Java API for Cloneable. Choice of deep vs shallow copy is also to be discussed.
    CustomGraph<N, E> copy();

}
