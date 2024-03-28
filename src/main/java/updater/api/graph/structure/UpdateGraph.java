package updater.api.graph.structure;

import util.api.CustomGraph;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Interface for graphs used in dependency update.
 */
public interface UpdateGraph<N extends UpdateNode, E extends UpdateEdge> extends CustomGraph<N, E> {

    /**
     * Artifact nodes. Default method than can be refined in classes for optimizing. 
     * @return the set of nodes that are artifact nodes (empty if none).
     */
    default Set<N> artifactNodes() {
        return this.nodes(N::isArtifact);
    }

    /**
     * Release nodes. Default method than can be refined in classes for optimizing.
     * @return the set of nodes that are release nodes (empty if none).
     */
    default Set<N> releaseNodes() {
        return this.nodes(N::isRelease);
    }

    /**
     * Version edges. Default method than can be refined in classes for optimizing.
     * @return the set of edges that are version edges (empty if none).
     */
    default Set<E> versionEdges() {
        return this.edges(E::isVersion);
    }

    /**
     * Dependency edges. Default method than can be refined in classes for optimizing.
     * @return the set of edges that are dependency edges (empty if none).
     * @return
     */
    default Set<E> dependencyEdges() {
        return this.edges(E::isDependency);
    }

    /**
     * Change edges. Default method than can be refined in classes for optimizing.
     * @return the set of edges that are change edges (empty if none).
     */
    default Set<E> changeEdges() {
        return this.edges(E::isChange);
    }

    /**
     * Direct dependencies of a node (should be a release node). Default method than can be refined in classes for optimizing.
     * @param node the node to find the direct dependencies of (should be a release node)
     * @return the set of nodes that are direct dependencies of node (empty if none).
     */
    default Set<N> directDependencies(N node) {
        return outgoingEdgesOf(node).stream()
               .filter(UpdateEdge::isDependency)
               .map(e -> target(e))
               .collect(Collectors.toSet());
    }

    /**
     * Direct dependencies of the root node. Default method than can be refined in classes for optimizing.
     * @return the of nodes that are direct dependencies the root (empty if none).
     */
    default Set<N> rootDirectDependencies() {
        return rootNode().map(this::directDependencies).orElse(Set.of());
    }

    /**
     * Versions of a node (should be an artifact node). Default method than can be refined in classes for optimizing.
     * @param node the node to find the versions of (should be an artifact node)
     * @return the set of nodes that are versions of node (empty if none).
     */
    default Set<N> versions(N node) {
        return outgoingEdgesOf(node).stream()
               .filter(UpdateEdge::isVersion)
               .map(e -> target(e))
               .collect(Collectors.toSet());
    }

    /**
     * Set of releases that are related to a node (should be a release node) by change edges. Default method than can be refined in classes for optimizing.
     * @param node the node to find the releases that are related to (should be a release node) by change edges
     * @return the set of nodes that are releases that are related to node (empty if none).
     */
    default Set<E> changes(N node) {
        return outgoingEdgesOf(node).stream()
               .filter(UpdateEdge::isChange)
               .collect(Collectors.toSet());
    }

    /**
     * TODO:
     * @param release
     * @param artifact
     * @return
     */
    Optional<N> currentDependencyRelease(N release, N artifact);

    /**
     * TODO:
     * @param artifact
     * @return
     */
    default Optional<N> rootCurrentDependencyRelease(N artifact) {
        return rootNode().flatMap(r -> currentDependencyRelease(r, artifact));
    }

    /**
     * Artifact of a node (should be a release node). Default method than can be refined in classes for optimizing.
     * @param node the node to find the versions of (should be a release node)
     * @return An optional node of the artifact corresponding to the release.
     */
    default Optional<N> artifactOf(N node){
        return incomingEdgesOf(node).stream()
                .filter(UpdateEdge::isVersion).findFirst().map(this::source);
    }

    /**
     * TODO:
     */
    UpdateGraph<N, E> copy();

}
