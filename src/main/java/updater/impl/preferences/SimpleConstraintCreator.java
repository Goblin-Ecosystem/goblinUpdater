package updater.impl.preferences;

import java.util.Optional;

import updater.api.preferences.Constraint;
import updater.api.preferences.ConstraintCreator;

public class SimpleConstraintCreator implements ConstraintCreator {

    private SimpleConstraintCreator() {
    }

    private static final ConstraintCreator instance = new SimpleConstraintCreator();

    public static ConstraintCreator instance() {
        return instance;
    }

    @Override
    public Optional<Constraint> create(String type, Object value) {
        return switch (type) {
            case "ABSENCE" -> (value instanceof String s) ? Optional.of(new AbsenceConstraint(s)) : Optional.empty();
            case "PRESENCE" -> (value instanceof String s) ? Optional.of(new PresenceConstraint(s)) : Optional.empty();
            case "COSTLIMIT" ->
                (value instanceof Double n) ? Optional.of(new CostLimitConstraint(n)) : Optional.empty();
            default -> Optional.empty();
        };
    }

}
