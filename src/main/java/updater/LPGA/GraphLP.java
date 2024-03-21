package updater.lpga;

import graph.structures.CustomGraph;
import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

public class GraphLP {

    private GraphLP() {}

    private static final String NR_PREFIX = "rel_";
    private static final String NA_PREFIX = "art_";
    private static final String ED_PREFIX = "dep_";
    private static final String EP_PREFIX = "pos_";

    public static <N extends UpdateNode> String nodeVariableName(N n, String prefix) {
        return prefix + n.id();
    }

    public static <N extends UpdateNode> String artifactVariableName(N n) {
        return nodeVariableName(n, NA_PREFIX);
    }

    public static <N extends UpdateNode> String releaseVariableName(N n) {
        return nodeVariableName(n, NR_PREFIX);
    }

    public static <N extends UpdateNode> MPVariable artifactVariable(MPSolver s, N n) {
        return s.lookupVariableOrNull(artifactVariableName(n));
    }

    public static <N extends UpdateNode> MPVariable releaseVariable(MPSolver s, N n) {
        return s.lookupVariableOrNull(releaseVariableName(n));
    }

    public static <N extends UpdateNode, E extends UpdateEdge> String edgeVariableName(CustomGraph<N, E> g, E e, String prefix) {
        return prefix + e.id() + "_" + g.source(e).id() + "->" + g.target(e).id();
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
