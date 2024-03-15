package updater;

import bazarRefonte.UpdatePreferences;
import project.Project;

import java.util.Optional;

public interface Updater {
    Optional<Project> update(Project project, UpdatePreferences updatePreferences);
}
