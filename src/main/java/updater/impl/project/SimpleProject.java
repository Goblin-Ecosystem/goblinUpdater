package updater.impl.project;

import java.nio.file.Path;
import java.util.Set;

import updater.api.project.Dependency;
import updater.api.project.Project;

/**
 * A simple implementation of {@link Project}.
 */
public class SimpleProject implements Project {
    private final Path path;
    private final Set<Dependency> directDependencies;

    public SimpleProject(Path path, Set<Dependency> directDependencies) {
        this.path = path;
        this.directDependencies = directDependencies;
    }

    // TODO: implement.
    @Override
    public void dump(Path path) {
    }

    @Override
    public Set<Dependency> getDirectDependencies() {
        return directDependencies;
    }

    @Override
    public Path getPath() {
        return path;
    }
}
