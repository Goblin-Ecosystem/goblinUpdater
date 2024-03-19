package project.maven;

import java.nio.file.Path;
import java.util.Set;

import project.Dependency;
import project.Project;

public class MavenProject implements Project {
    private final Path path;
    //TODO: ne pas utiliser la classe dependency de maven
    private final Set<Dependency> directDependencies;

    public MavenProject(Path path, Set<Dependency> directDependencies) {
        this.path = path;
        this.directDependencies = directDependencies;
    }

    @Override
    public void dump(Path path) {
        //TODO
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
