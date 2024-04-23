package client;

import updater.api.preferences.Preferences;
import updater.api.process.Updater;
import updater.api.project.Project;
import updater.api.project.ProjectLoader;
import updater.impl.maven.project.MavenProjectLoader;
import updater.impl.preferences.SimplePreferences;
import updater.impl.process.graphbased.lpga.LPGAUpdater;
import util.helpers.system.LoggerHelpers;
import util.helpers.system.LoggerHelpers.Level;
import util.helpers.system.MemoryUsageTracker;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Basic client for LPGA project update in the Maven/Maven Central eco-system.
 */
public class ClientLPGA {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        LoggerHelpers.instance().setLevel(Level.INFO);
        Path projectPath = Path.of(System.getProperty("projectPath"));
        Path preferencesPath = Path.of(System.getProperty("confFile"));
        Path updatePath = Path.of("..");

        Preferences preferences = new SimplePreferences(preferencesPath);
        ProjectLoader loader = new MavenProjectLoader();
        Updater updater = new LPGAUpdater();

        preferences.print();
        Optional<Project> updatedProject = loader
                .load(projectPath)
                .flatMap(project -> updater.update(project, preferences));
        MemoryUsageTracker.getInstance().checkAndUpdateMaxMemoryUsage();
        if (updatedProject.isPresent())
            updatedProject.get().dump(updatePath);
        else
            LoggerHelpers.instance().error("Could not update project");
        MemoryUsageTracker.getInstance().printMemoryUsageMax();
        long endTime = System.currentTimeMillis();
        LoggerHelpers.instance().info("Total execution time (ms): "+ (endTime - startTime));
    }
}
