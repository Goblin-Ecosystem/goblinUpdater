package updater.api.graph;

import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import util.api.CustomGraph;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface UpdateGraph<N extends UpdateNode, E extends UpdateEdge> extends CustomGraph<N, E> {

    default Set<N> artifactNodes() {
        return this.nodes(N::isArtifact);
    }

    default Set<N> releaseNodes() {
        return this.nodes(N::isRelease);
    }

    default Set<E> versionEdges() {
        return this.edges(E::isVersion);
    }

    default Set<E> dependencyEdges() {
        return this.edges(E::isDependency);
    }

    default Set<E> changeEdges() {
        return this.edges(E::isChange);
    }

    default Set<N> directDependencies(N node) {
        return outgoingEdgesOf(node).stream()
               .filter(UpdateEdge::isDependency)
               .map(e -> target(e))
               .collect(Collectors.toSet());
    }

    default Set<N> rootDirectDependencies() {
        return rootNode().map(this::directDependencies).orElse(Set.of());
    }

    default Set<N> versions(N node) {
        return outgoingEdgesOf(node).stream()
               .filter(UpdateEdge::isVersion)
               .map(e -> target(e))
               .collect(Collectors.toSet());
    }

    default Set<E> possibles(N node) {
        return outgoingEdgesOf(node).stream()
               .filter(UpdateEdge::isChange)
               .collect(Collectors.toSet());
    }

    Optional<N> currentDependencyRelease(N release, N artifact);

    default Optional<N> rootCurrentDependencyRelease(N artifact) {
        return rootNode().flatMap(r -> currentDependencyRelease(r, artifact));
    }

    UpdateGraph<N, E> copy();

}
