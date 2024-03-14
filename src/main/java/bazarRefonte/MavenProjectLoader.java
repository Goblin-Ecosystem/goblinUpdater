package bazarRefonte;

import java.nio.file.Path;

public class MavenProjectLoader implements ProjectLoader {

    public MavenProjectLoader() {
    }

    @Override
    public Project load(Path path) {
        return new MavenProject();
    }
}
