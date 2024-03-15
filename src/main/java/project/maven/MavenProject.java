package project.maven;

import java.nio.file.Path;
import java.util.Set;
import org.apache.maven.model.Dependency;
import project.Project;

public class MavenProject implements Project {
    private final Set<Dependency> directDependencies;

    public MavenProject(Set<Dependency> directDependencies) {
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
}
