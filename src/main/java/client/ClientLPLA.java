package client;

import updater.api.process.Updater;
import updater.api.project.Project;
import updater.api.project.ProjectLoader;
import updater.impl.maven.project.MavenProjectLoader;
import updater.impl.preferences.SimplePreferences;
import updater.impl.process.graphbased.lpla.LPLAUpdater;
import util.helpers.system.LoggerHelpers;
import util.helpers.system.LoggerHelpers.Level;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Basic client for LPLA project update in the Maven/Maven Central eco-system.
 */
public class ClientLPLA {

    public static void main(String[] args) {
        LoggerHelpers.instance().setLevel(Level.INFO);
        Path projectPath = Path.of(System.getProperty("projectPath"));
        Path preferencesPath = Path.of(System.getProperty("confFile"));
        Path updatePath = Path.of("..");

        ProjectLoader loader = new MavenProjectLoader();
        Updater updater = new LPLAUpdater();

        Optional<Project> updatedProject = loader
                .load(projectPath)
                .flatMap(project -> updater.update(project,
                        new SimplePreferences(preferencesPath)));

        if (updatedProject.isPresent())
            updatedProject.get().dump(updatePath);
        else
            LoggerHelpers.instance().error("Could not update project");
    }
}
