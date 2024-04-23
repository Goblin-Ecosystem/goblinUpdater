package updater.impl.preferences;

import updater.api.preferences.Constraint;

public record CveLimitConstraint(double limit) implements Constraint<Double> {

    @Override
    public boolean isFocus() {
        return false;
    }

    @Override
    public String focus() {
        return null;
    }

    @Override
    public Double value() {
        return limit;
    }

    @Override
    public String code() {
        return "CVE_LIMIT";
    }

    @Override
    public String repr() {
        return String.format("%n  - constraint: %s%n    value: %s", code(), value());
    }
}
