package graph.entities.edges;

import org.jgrapht.graph.DefaultEdge;


public abstract class CustomEdge extends DefaultEdge {
    private final EdgeType type;

    public CustomEdge(EdgeType type) {
        this.type = type;
    }

    public EdgeType getType() {
        return type;
    }

}
