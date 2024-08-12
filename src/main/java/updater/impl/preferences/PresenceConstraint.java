package updater.impl.preferences;

import updater.api.preferences.Constraint;

public record PresenceConstraint(String id) implements Constraint<String> {

    @Override
    public boolean isFocus() {
        return false;
    }

    @Override
    public String focus() {
        return null;
    }

    @Override
    public String value() {
        return id;
    }

    @Override
    public String code() {
        return "PRESENCE";
    }

    @Override
    public String repr() {
        return String.format("%n  - constraint: %s%n    value: \"%s\"", code(), value());
    }
}
