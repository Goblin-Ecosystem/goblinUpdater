package updater.impl.preferences;

import updater.api.preferences.Constraint;

public record AbsenceConstraint(String id) implements Constraint {

    @Override
    public boolean isFocus() {
        return true;
    }

    @Override
    public String focus() {
        return id;
    }
}
