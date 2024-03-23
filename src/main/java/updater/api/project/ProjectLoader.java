package updater.api.project;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Interface for loading a project from a path.
 */
public interface ProjectLoader {
    /**
     * Loads the project at the given path. It may fail if the project is not valid
     * or does not exist.
     * 
     * @param projectPath the path to the project
     */
    Optional<Project> load(Path projectPath);
}
