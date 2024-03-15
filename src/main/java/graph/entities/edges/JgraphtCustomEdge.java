package graph.entities.edges;

import org.jgrapht.graph.DefaultEdge;


public abstract class JgraphtCustomEdge extends DefaultEdge implements UpdateEdge{
    private final EdgeType type;

    public JgraphtCustomEdge(EdgeType type) {
        this.type = type;
    }

    public EdgeType getType() {
        return type;
    }

    public String name(){
        return type.toString();
    }
}
