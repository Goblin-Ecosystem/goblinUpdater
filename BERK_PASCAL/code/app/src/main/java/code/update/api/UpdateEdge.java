package code.update.api;

public interface UpdateEdge {
    String name();
    boolean isVersion();
    boolean isDependency();
    boolean isPossible();
}
