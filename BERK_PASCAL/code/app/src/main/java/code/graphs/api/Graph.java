package code.graphs.api;

import java.util.List;
import java.util.function.Predicate;
import java.util.Optional;

/**
 * Graph is generic on N (node type) and E (edge type)
 */ 
public interface Graph<N, E> {

    List<N> nodes();

    List<E> edges();

    default List<N> nodes(Predicate<N> p) {
        return nodes().stream().filter(p).toList();
    }

    default List<E> edges(Predicate<E> p) {
        return edges().stream().filter(p).toList();
    }

    N source(E e);

    N target(E e);

    Optional<N> rootNode();
}
