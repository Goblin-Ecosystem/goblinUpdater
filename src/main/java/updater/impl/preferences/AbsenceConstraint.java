package updater.impl.preferences;

import updater.api.preferences.Constraint;

public record AbsenceConstraint(String id) implements Constraint<String> {

    @Override
    public boolean isFocus() {
        return true;
    }

    @Override
    public String focus() {
        return id;
    }

    @Override
    public String value() {
        return id;
    }

    @Override
    public String code() {
        return "ABSENCE";
    }

    @Override
    public String repr() {
        return String.format("%n  - constraint: %s%n    value: \"%s\"", code(), value());
    }
}
