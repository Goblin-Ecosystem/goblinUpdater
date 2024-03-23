package client;

import updater.api.process.Updater;
import updater.api.project.Project;
import updater.api.project.ProjectLoader;
import updater.impl.maven.project.MavenProjectLoader;
import updater.impl.preferences.SimplePreferences;
import updater.impl.process.graphbased.lpga.LPGAUpdater;
import util.helpers.system.LoggerHelpers;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Basic client for LPGA project update in the Maven/Maven Central eco-system.
 */
public class ClientLPGA {

    public static void main(String[] args) {
        Path projectPath = Path.of(System.getProperty("projectPath"));
        Path preferencesPath = Path.of(System.getProperty("confFile"));
        Path updatePath = Path.of("..");

        ProjectLoader loader = new MavenProjectLoader();
        Updater updater = new LPGAUpdater();

        Optional<Project> updatedProject = loader
                .load(projectPath)
                .flatMap(project -> updater.update(project,
                        new SimplePreferences(preferencesPath)));

        if (updatedProject.isPresent())
            updatedProject.get().dump(updatePath);
        else
            LoggerHelpers.error("Could not update project");
    }
}
