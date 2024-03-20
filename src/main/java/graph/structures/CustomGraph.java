package graph.structures;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface CustomGraph<N, E> {
    void addNode(N node);

    void addEdgeFromNodeId(String fromId, String toId, E edge);

    void removeNode(N node);

    Set<N> nodes();

    Set<E> edges();

    default Set<N> nodes(Predicate<N> p) {
        return nodes().stream().filter(p).collect(Collectors.toSet());
    }

    default Set<E> edges(Predicate<E> p) {
        return edges().stream().filter(p).collect(Collectors.toSet());
    }

    N source(E edge);

    N target(E edge);

    Optional<N> rootNode();

    Set<E> outgoingEdgesOf(N node);

    CustomGraph<N, E> copy();

}
