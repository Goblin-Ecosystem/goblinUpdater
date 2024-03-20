package project.maven;

import project.Dependency;
import project.Project;
import project.ProjectLoader;
import util.LoggerHelpers;
import util.SystemHelpers;

import java.nio.file.Path;
import java.util.*;

public class MavenProjectLoader implements ProjectLoader {

    @Override
    public Project load(Path projectPath) {
        LoggerHelpers.info("Get pom direct dependencies");
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
                // TODO: scope filter ???
                resultList.add(new Dependency(groupId, artifactId, version));

            }
        }
        LoggerHelpers.info("Direct dependencies number: " + resultList.size());
        return new MavenProject(projectPath, resultList);
    }
}
