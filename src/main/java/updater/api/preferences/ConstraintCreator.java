package updater.api.preferences;

import java.util.Optional;

public interface ConstraintCreator {
    Optional<Constraint> create(String type, Object value);
}
