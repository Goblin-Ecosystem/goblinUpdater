package bazarRefonte;

import java.nio.file.Path;
import java.util.Set;
import java.util.HashSet;
import org.apache.maven.model.Dependency;

public class MavenProject implements Project {

    Set<Dependency> dependencies = new HashSet<>();

    @Override
    public void dump(Path path) {
    }
}
