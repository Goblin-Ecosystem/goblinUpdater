package updater.impl.preferences;

import updater.api.preferences.Constraint;

public record CostLimitConstraint(double limit) implements Constraint {

    @Override
    public boolean isFocus() {
        return false;
    }

    @Override
    public String focus() {
        return null;
    }
}
