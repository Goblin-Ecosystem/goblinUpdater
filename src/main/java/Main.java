import addedvalue.AddedValueEnum;
import graph.generator.GraphGenerator;
import graph.generator.JgraphtGraphGenerator;
import graph.structures.GraphStructure;
import org.apache.maven.model.Dependency;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.json.simple.JSONObject;
import util.GoblinWeaverHelpers;
import util.LoggerHelpers;
import util.MavenHelpers;

import java.io.IOException;
import java.util.List;

public class Main {

    // Direct all possibilities & transitives dependencies
    public static void main(String[] args){
        String projectPath = args[0];
        try {
            // Get pom direct dependencies
            List<Dependency> pomDependencies = MavenHelpers.getProjectDirectDependencies(projectPath);
            // TODO addedValues via conf file
            List<AddedValueEnum> addedValuesToCompute = List.of(AddedValueEnum.CVE, AddedValueEnum.FRESHNESS);
            JSONObject jsonDirectPossibilitiesRootedGraph = GoblinWeaverHelpers.getDirectPossibilitiesWithTransitiveRootedGraph(pomDependencies, addedValuesToCompute);
            // Transform Json to JgraphT graph
            GraphGenerator graphGenerator = new JgraphtGraphGenerator();
            GraphStructure graph = graphGenerator.generateRootedGraphFromJsonObject(jsonDirectPossibilitiesRootedGraph, addedValuesToCompute);
            graph.generateChangeEdge(projectPath);
        } catch (IOException | XmlPullParserException e) {
            LoggerHelpers.fatal(e.getMessage());
        }
    }
}
