package client;

import bazarRefonte.MavenPreferences;
import project.Project;
import project.ProjectLoader;
import project.maven.MavenProjectLoader;
import updater.LPLA.MavenLPLAUpdater;
import updater.Updater;

import java.nio.file.Path;
import java.util.Optional;

/*
    LPLA (Local Possible, Local Analysis)
 */
public class ClientLPLA {

    public static void main(String[] args){
        ProjectLoader loader = new MavenProjectLoader();
        Project project = loader.load(Path.of(System.getProperty("projectPath")));
        Updater updater = new MavenLPLAUpdater();
        //TODO: Paths
        Optional<Project> updatedProject = updater.update(project, new MavenPreferences(Path.of("...")));
        updatedProject.ifPresent(up -> up.dump(Path.of("..")));
    }
}
