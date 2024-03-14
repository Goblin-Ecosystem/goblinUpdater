package bazarRefonte;

import java.nio.file.Path;

public interface ProjectLoader {
    Project load(Path path);
}
