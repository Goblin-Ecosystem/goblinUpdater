package updater.api.preferences;

public interface Constraint<T> {
    /**
     * Returns whether the constraint is related to a focus or not.
     */
    boolean isFocus();

    /**
     * Returns the focus of the constraint.
     */
    String focus();

    /**
     * Returns the code of the constraint.
     */
    String code();

    /**
     * Returns the value of the constraint.
     */
    T value();

    /**
     * Returns the representation.
     */
    String repr();
}
