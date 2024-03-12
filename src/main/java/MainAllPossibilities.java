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
import util.YmlConfReader;

import java.io.IOException;
import java.util.Set;

public class MainAllPossibilities {

    // Graph all prosibilities
    public static void mainAllPossibilities(String[] args){
        String projectPath = System.getProperty("projectPath");
        try {
            // Get pom direct dependencies
            Set<Dependency> pomDependencies = MavenHelpers.getProjectDirectDependencies(projectPath);
            Set<AddedValueEnum> addedValuesToCompute = YmlConfReader.getInstance().getAddedValueEnumSet();
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
