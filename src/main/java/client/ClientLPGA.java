package client;

import bazarRefonte.*;
import project.maven.MavenProjectLoader;
import project.Project;
import project.ProjectLoader;
import updater.LPGA.MavenLPGAUpdater;
import updater.Updater;

import java.nio.file.Path;
import java.util.Optional;

/*
    LPGA (Local Possible, Global Analysis)
 */
public class ClientLPGA {

    public static void main(String[] args){
        ProjectLoader loader = new MavenProjectLoader();
        Project project = loader.load(Path.of(System.getProperty("projectPath")));
        Updater updater = new MavenLPGAUpdater();
        Optional<Project> updatedProject = updater.update(project, new MavenPreferences(Path.of("...")));
        updatedProject.ifPresent(up -> up.dump(Path.of("..")));
    }
}
