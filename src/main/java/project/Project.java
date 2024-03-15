package project;

import org.apache.maven.model.Dependency;

import java.nio.file.Path;
import java.util.Set;

public interface Project {
    //TODO: la classe Dependency est Maven specific (créer interface Dependency et impl Maven)
    Set<Dependency> getDirectDependencies();
    Path getPath();
    void dump(Path path);
}
