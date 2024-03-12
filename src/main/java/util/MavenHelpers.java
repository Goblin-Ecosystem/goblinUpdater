package util;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class MavenHelpers {

    public static List<Dependency> getPomDirectDependencies(String pomPath) throws IOException, XmlPullParserException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader(pomPath));
        return model.getDependencies();
    }

    public static List<Dependency> getProjectDirectDependencies(String projectPath) throws IOException, XmlPullParserException {
        LoggerHelpers.info("Get pom direct dependencies");
        List<Dependency> resultList = new ArrayList<>();
        //TODO Windows specific
        List<String> lines = SystemHelpers.execCommand("cd /d "+projectPath.replace("/","\\")+ " && mvn dependency:list -DexcludeTransitive=true");
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
        return resultList;
    }
}
