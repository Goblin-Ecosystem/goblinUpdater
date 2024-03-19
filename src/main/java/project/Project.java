package project;

import java.nio.file.Path;
import java.util.Set;

public interface Project {
    Set<Dependency> getDirectDependencies();
    Path getPath();
    void dump(Path path);
}
