package updater.impl.preferences;

import updater.api.preferences.Constraint;

public record PresenceConstraint(String id) implements Constraint {

    @Override
    public boolean isFocus() {
        return false;
    }

    @Override
    public String focus() {
        return null;
    }
}
