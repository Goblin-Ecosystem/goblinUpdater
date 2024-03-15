package project.maven;

import org.apache.maven.model.Dependency;
import project.Project;
import project.ProjectLoader;
import util.LoggerHelpers;
import util.SystemHelpers;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MavenProjectLoader implements ProjectLoader {

    public MavenProjectLoader() {

    }

    @Override
    public Project load(Path projectPath) {
        LoggerHelpers.info("Get pom direct dependencies");
        Set<Dependency> resultList = new HashSet<>();
        //TODO Windows specific
        List<String> lines = SystemHelpers.execCommand("cd /d "+projectPath.toString().replace("/","\\")+ " && mvn dependency:list -DexcludeTransitive=true");
        Iterator<String> lineIterator = lines.iterator();
        String line = lineIterator.next();
        while(lineIterator.hasNext() && !line.contains("The following files have been resolved:")){
            line = lineIterator.next();
        }
        while(lineIterator.hasNext() && !line.contains("BUILD SUCCESS")){
            line = lineIterator.next();
            if (line.matches(".*:.+:.+:.+:.+")) {
                String[] parts = line.split(":");
                String groupId = parts[0].split("]")[1].trim();
                String artifactId = parts[1].trim();
                String version = parts[3].trim();
                String scope = parts[4].trim();
                //TODO scope filter ???
                Dependency dep = new Dependency();
                dep.setGroupId(groupId);
                dep.setArtifactId(artifactId);
                dep.setVersion(version);
                resultList.add(dep);

            }
        }
        LoggerHelpers.info("Direct dependencies number: "+resultList.size());
        return new MavenProject(resultList);
    }
}
