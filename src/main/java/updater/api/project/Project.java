package updater.api.project;

import java.nio.file.Path;
import java.util.Set;

/**
 * Interface for a project. The minimal set of operations required for a project
 * is getting its direct dependencies, getting its path and "dumping" it to disk (which may mean only saving a dependency file or more).
 */
public interface Project {
    /**
     * Returns the direct dependencies of this project. Empty set if there are none.
     * 
     * @return the direct dependencies of the project.
     */
    Set<Dependency> getDirectDependencies();

    /**
     * Returns the path to this project.
     * @return the path to the project
     */
    Path getPath();

    /**
     * Dumps the project to disk (which may mean only saving a dependency file or more).
     * @param path the path to save the project to.
     */
    void dump(Path path);
}
