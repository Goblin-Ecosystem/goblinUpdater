package graph.structures;

import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;

import java.util.Set;

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

    Set<E> getPossibleEdgesOf(N node);

    N getCurrentUseReleaseOfArtifact(N artifact);

    Set<N> getRootArtifactDirectDep();

    Set<N> getAllArtifactRelease(N artifact);

    UpdateGraph<N, E> copy();

}
