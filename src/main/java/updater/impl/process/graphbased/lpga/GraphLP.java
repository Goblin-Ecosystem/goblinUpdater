package updater.impl.process.graphbased.lpga;

import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;

public class GraphLP {

    private GraphLP() {
    }

    public static final String COST_VARIABLE_NAME = "COST";
    public static final String CVE_VARIABLE_NAME = "Quality[CVE]";
    public static final String QUALITY_VARIABLE_NAME = "QUALITY";

    private static final String NA_PATTERN = "[%s]";
    private static final String NR_PATTERN = "(%s)";
    private static final String ED_ARROW = "-->";
    private static final String EC_ARROW = "==>";

    public static <N extends UpdateNode> String nodeVariableName(N n, String pattern) {
        return String.format(pattern, n.id());
    }

    public static <N extends UpdateNode> String artifactVariableName(N n) {
        return nodeVariableName(n, NA_PATTERN);
    }

    public static <N extends UpdateNode> String releaseVariableName(N n) {
        return nodeVariableName(n, NR_PATTERN);
    }

    public static <N extends UpdateNode> MPVariable artifactVariable(MPSolver s, N n) {
        return s.lookupVariableOrNull(artifactVariableName(n));
    }

    public static <N extends UpdateNode> MPVariable releaseVariable(MPSolver s, N n) {
        return s.lookupVariableOrNull(releaseVariableName(n));
    }

    public static MPVariable totalCostVariable(MPSolver s) {
        return s.lookupVariableOrNull(COST_VARIABLE_NAME);
    }

    public static MPVariable totalCveVariable(MPSolver s) {
        return s.lookupVariableOrNull(CVE_VARIABLE_NAME);
    }

    public static MPVariable totalQualityVariable(MPSolver s) {
        return s.lookupVariableOrNull(QUALITY_VARIABLE_NAME);
    }

    public static <N extends UpdateNode, E extends UpdateEdge> String edgeVariableName(UpdateGraph<N, E> g, E e,
            String arrow) {
        return String.format("%s%s%s (%s)", g.source(e).id(), arrow, g.target(e).id(), e.id());
    }

    public static <N extends UpdateNode, E extends UpdateEdge> String dependencyVariableName(UpdateGraph<N, E> g, E e) {
        return edgeVariableName(g, e, ED_ARROW);
    }

    public static <N extends UpdateNode, E extends UpdateEdge> String changeVariableName(UpdateGraph<N, E> g, E e) {
        return edgeVariableName(g, e, EC_ARROW);
    }

    public static <N extends UpdateNode, E extends UpdateEdge> MPVariable dependencyVariable(MPSolver s,
            UpdateGraph<N, E> g, E e) {
        return s.lookupVariableOrNull(dependencyVariableName(g, e));
    }

    public static <N extends UpdateNode, E extends UpdateEdge> MPVariable changeVariable(MPSolver s,
            UpdateGraph<N, E> g, E e) {
        return s.lookupVariableOrNull(changeVariableName(g, e));
    }

}
