package bazarRefonte;

public interface UpdateEdge {
    String name();
    boolean isVersion();
    boolean isDependency();
    boolean isPossible(); // Change link
}
