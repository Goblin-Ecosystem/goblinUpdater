package updater.api.process;

import java.util.Optional;

import updater.api.preferences.Preferences;
import updater.api.project.Project;

/**
 * Interface for project updaters. A project updater is responsible for updating
 * a given project according to some user preferences.
 */
public interface Updater {
    /**
     * Updates the given project with the given update preferences.
     * 
     * @param project           The project to be updated.
     * @param updatePreferences The update preferences used for updating the
     *                          project.
     * @return An optional containing the updated project if successful or empty
     *         otherwise.
     */
    Optional<Project> update(Project project, Preferences updatePreferences);
}
