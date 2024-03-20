package graph.entities.edges;

import org.jgrapht.graph.DefaultEdge;

public abstract class AbstractEdge extends DefaultEdge implements UpdateEdge {

    private String id;

    protected AbstractEdge(String id) {
        super();
        this.id = id;
    }

    @Override
    public String id() {
        return this.id;
    }
}
