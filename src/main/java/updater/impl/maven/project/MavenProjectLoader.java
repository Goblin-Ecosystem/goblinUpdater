package updater.impl.maven.project;

import updater.api.project.Dependency;
import updater.api.project.Project;
import updater.api.project.ProjectLoader;
import updater.impl.project.SimpleProject;
import util.helpers.system.LoggerHelpers;
import util.helpers.system.SystemHelpers;

import java.nio.file.Path;
import java.util.*;

/**
 * A loader for a project in the Maven/Maven Central eco-system.
 */
public class MavenProjectLoader implements ProjectLoader {

    @Override
    public Optional<Project> load(Path projectPath) {
        LoggerHelpers.instance().info("Get pom direct dependencies");
        Set<Dependency> resultList = new HashSet<>();
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(projectPath.toFile());
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            processBuilder.command("cmd", "/c", "mvn", "dependency:list", "-DexcludeTransitive=true");
        } else {
            processBuilder.command("/bin/sh", "-c", "mvn", "dependency:list", "-DexcludeTransitive=true");
        }
        List<String> lines = SystemHelpers.execCommand(processBuilder);
        Iterator<String> lineIterator = lines.iterator();
        String line = lineIterator.next();
        while (lineIterator.hasNext() && !line.contains("The following files have been resolved:")) {
            line = lineIterator.next();
        }
        while (lineIterator.hasNext() && !line.contains("BUILD SUCCESS")) {
            line = lineIterator.next();
            if (line.matches(".*:.+:.+:.+:.+")) {
                String[] parts = line.split(":");
                String groupId = parts[0].split("]")[1].trim();
                String artifactId = parts[1].trim();
                String version = parts[3].trim();
                String scope = parts[4].trim();
                // TODO: integrate scopes (filtering).
                resultList.add(new Dependency(groupId, artifactId, version));
            }
        }
        LoggerHelpers.instance().info("Direct dependencies number: " + resultList.size());
        return Optional.of(new SimpleProject(projectPath, resultList));
    }
}
