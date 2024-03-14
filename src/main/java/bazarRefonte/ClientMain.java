package bazarRefonte;

import java.nio.file.Path;
import java.util.Optional;

public class ClientMain {

    public static void main(String[] args){
        ProjectLoader loader = new MavenProjectLoader();
        Project project = loader.load(Path.of("..."));
        Updater updater = new MavenLPGAUpdater();
        Optional<Project> updatedProject = updater.update(project, new MavenPreferences(Path.of("...")));
        updatedProject.ifPresent(up -> up.dump(Path.of("..")));
    }
}
