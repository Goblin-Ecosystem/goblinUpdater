package updater.api.project;

import java.nio.file.Path;
import java.util.Set;

/**
 * Interface for a project. The minimal set of operations required for a project
 * is getting its direct dependencies, getting its path and dumping it to disk.
 */
public interface Project {
    /**
     * Returns the direct dependencies of this project. Empty set if there are none.
     * 
     * @return the direct dependencies of the project.
     */
    Set<Dependency> getDirectDependencies();

    Path getPath();

    void dump(Path path);
}
