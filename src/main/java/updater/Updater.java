package updater;

import updater.updatePreferences.UpdatePreferences;
import project.Project;

import java.util.Optional;

public interface Updater {
    Optional<Project> update(Project project, UpdatePreferences updatePreferences);
}
