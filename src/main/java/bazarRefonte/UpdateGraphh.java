package bazarRefonte;

import java.util.List;

public interface UpdateGraphh<N extends UpdateNode, E extends UpdateEdge> extends Graphh<N, E>{

    default List<N> libraryNodes(){
        return this.nodes(N::isLibrary);
    }

    default List<N> artifactNodes(){
        return this.nodes(N::isArtifact);
    }

    default List<E> versionEdges(){ return this.edges(E::isVersion);}
    default List<E> dependencyEdges(){ return this.edges(E::isDependency);}
    default List<E> possibleEdges(){ return this.edges(E::isPossible);}
}
