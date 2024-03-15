package updater.LPGA;

import graph.structures.CustomGraph;
import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

public class GraphLP {

    private GraphLP() {}

    private static final String NA_PREFIX = "x_";
    private static final String NL_PREFIX = "y_";
    private static final String ED_PREFIX = "d_";
    private static final String EP_PREFIX = "p_";

    public static <N extends UpdateNode> String nodeVariableName(N n, String prefix) {
        return prefix + n.getId();
    }

    public static <N extends UpdateNode> String artifactVariableName(N n) {
        return nodeVariableName(n, NA_PREFIX);
    }

    public static <N extends UpdateNode> String libraryVariableName(N n) {
        return nodeVariableName(n, NL_PREFIX);
    }

    public static <N extends UpdateNode> MPVariable artifactVariable(MPSolver s, N n) {
        return s.lookupVariableOrNull(artifactVariableName(n));
    }

    public static <N extends UpdateNode> MPVariable libraryVariable(MPSolver s, N n) {
        return s.lookupVariableOrNull(libraryVariableName(n));
    }

    public static <N extends UpdateNode, E extends UpdateEdge> String edgeVariableName(CustomGraph<N, E> g, E e, String prefix) {
        return prefix + e.name() + "_" + g.source(e).getId() + "->" + g.target(e).getId();
    }

    public static <N extends UpdateNode, E extends UpdateEdge>String dependencyVariableName(CustomGraph<N, E> g, E e) {
        return edgeVariableName(g, e, ED_PREFIX);
    }

    public static <N extends UpdateNode, E extends UpdateEdge>String possibilityVariableName(CustomGraph<N, E> g, E e) {
        return edgeVariableName(g, e, EP_PREFIX);
    }

    public static <N extends UpdateNode, E extends UpdateEdge>MPVariable dependencyVariable(MPSolver s, CustomGraph<N, E> g, E e) {
        return s.lookupVariableOrNull(dependencyVariableName(g, e));
    }

    public static <N extends UpdateNode, E extends UpdateEdge>MPVariable possibilityVariable(MPSolver s, CustomGraph<N, E> g, E e) {
        return s.lookupVariableOrNull(possibilityVariableName(g, e));
    }

}
