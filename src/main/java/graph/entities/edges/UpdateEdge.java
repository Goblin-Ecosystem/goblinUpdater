package graph.entities.edges;

import graph.structures.Identifiable;

public interface UpdateEdge extends Identifiable<String> {
    boolean isVersion();
    boolean isDependency();
    boolean isChange();
}
