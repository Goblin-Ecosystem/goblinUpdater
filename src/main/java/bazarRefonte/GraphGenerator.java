package bazarRefonte;

public interface GraphGenerator<N extends UpdateNode, E extends UpdateEdge> {
    UpdateGraphh<N,E> computeUpdateGraph(Project project, UpdatePreferences updatePreferences);
}
