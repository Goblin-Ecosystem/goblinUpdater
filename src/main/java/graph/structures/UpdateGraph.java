package graph.structures;

import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;

import java.util.Set;

public interface UpdateGraph<N extends UpdateNode, E extends UpdateEdge> extends CustomGraph<N, E> {

    default Set<N> libraryNodes(){
        return this.nodes(N::isLibrary);
    }

    default Set<N> releaseNodes(){
        return this.nodes(N::isRelease);
    }

    default Set<E> versionEdges(){ return this.edges(E::isVersion);}
    default Set<E> dependencyEdges(){ return this.edges(E::isDependency);}
    default Set<E> possibleEdges(){ return this.edges(E::isPossible);}

    Set<E> getPossibleEdgesOf(N node);

    N getCurrentUseReleaseOfArtifact(UpdateNode artifact);

    Set<UpdateNode> getRootArtifactDirectDep();

    Set<UpdateNode> getAllArtifactRelease(UpdateNode artifact);
}
