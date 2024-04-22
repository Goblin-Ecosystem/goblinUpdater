package updater.api.preferences;

public interface Constraint {
    /**
     * Returns whether the constraint is related to a focus or not.
     */
    boolean isFocus();

    /**
     * Return the focus of the constraint.
     */
    String focus();
}
