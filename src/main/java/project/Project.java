package project;

import org.apache.maven.model.Dependency;

import java.nio.file.Path;
import java.util.Set;

public interface Project {
    Set<Dependency> getDirectDependencies();
    void dump(Path path);
}
