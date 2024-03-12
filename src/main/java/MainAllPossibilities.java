import addedvalue.AddedValueEnum;
import graph.entities.nodes.ArtifactNode;
import graph.entities.nodes.ReleaseNode;
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
import java.util.Set;

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
            graph.generateChangeEdge();
            // compute direct dep cost
            LoggerHelpers.info("Compute cost");
            Set<ArtifactNode> artifactDirectDeps = graph.getRootArtifactDirectDep();
            for(ArtifactNode artifactDirectDep : artifactDirectDeps){
                // Get current used version
                ReleaseNode currentRelease = graph.getCurrentUseReleaseOfArtifact(artifactDirectDep);
                Set<ReleaseNode> allArtifactRelease = graph.getAllArtifactRelease(artifactDirectDep);
                for(ReleaseNode artifactRelease : allArtifactRelease){
                    // If quality of current release > artifact release, delete node
                    if(currentRelease.getNodeQuality() >= artifactRelease.getNodeQuality()){
                        graph.removeVertex(artifactRelease);
                    }
                    // Else compute change cost
                    else{
                        //artifactRelease.setChangeCost(MaracasHelpers.computeChangeCost(projectPath, currentRelease, artifactRelease));
                        //System.out.println(dtf.format(LocalDateTime.now())+"  "+artifactRelease.getId()+" quality: "+artifactRelease.getNodeQuality()+" cost: "+artifactRelease.getChangeCost());
                    }
                }
            }
            // Gurobi export

        } catch (IOException | XmlPullParserException e) {
            LoggerHelpers.fatal(e.getMessage());
        }
    }
}
