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

public class MainAllPossibilities {

    // Graph all prosibilities
    public static void mainAllPossibilities(String[] args){
        String projectPath = args[0];
        try {
            // Get pom direct dependencies
            List<Dependency> pomDependencies = MavenHelpers.getProjectDirectDependencies(projectPath);
            // TODO addedValues via conf file
            List<AddedValueEnum> addedValuesToCompute = List.of(AddedValueEnum.CVE, AddedValueEnum.FRESHNESS);
            JSONObject jsonAllPossibilitiesRootedGraph = GoblinWeaverHelpers.getAllPossibilitiesRootedGraph(pomDependencies, addedValuesToCompute);
            // Transform Json to JgraphT graph
            GraphGenerator graphGenerator = new JgraphtGraphGenerator();
            GraphStructure graph = graphGenerator.generateRootedGraphFromJsonObject(jsonAllPossibilitiesRootedGraph, addedValuesToCompute);
            // Create change edges
            graph.generateChangeEdge(projectPath);
            // Gurobi export

        } catch (IOException | XmlPullParserException e) {
            LoggerHelpers.fatal(e.getMessage());
        }
    }
}
