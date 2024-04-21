package updater.impl.preferences;

import updater.api.preferences.Constraint;

public record CostLimitConstraint(double limit) implements Constraint {
}
