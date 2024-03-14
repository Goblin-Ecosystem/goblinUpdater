package bazarRefonte;

import java.util.Optional;

public interface Updater {
    Optional<Project> update(Project project, UpdatePreferences updatePreferences);
}
