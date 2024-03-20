package updater;

import project.Project;
import updater.preferences.UpdatePreferences;

import java.util.Optional;

public interface Updater {
    Optional<Project> update(Project project, UpdatePreferences updatePreferences);
}
