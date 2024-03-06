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
import util.MavenHelpers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

public class MainAllPossibilities {

    public static void mainAllPossibilities(String[] args){
        // TODO project en argument
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String projectPath = "C:/Users/I542791/Desktop/expUpdate/goblinWeaver";
        try {
            // Get pom direct dependencies
            System.out.println(dtf.format(LocalDateTime.now())+" Get pom direct dependencies");
            List<Dependency> pomDependencies = MavenHelpers.getProjectDirectDependencies(projectPath);
            System.out.println(dtf.format(LocalDateTime.now())+" Direct dependencies number: "+pomDependencies.size());
            // TODO addedValues via conf file
            List<AddedValueEnum> addedValuesToCompute = List.of(AddedValueEnum.CVE, AddedValueEnum.FRESHNESS);
            System.out.println(dtf.format(LocalDateTime.now())+" Get all possibilities graph");
            JSONObject jsonAllPossibilitiesRootedGraph = GoblinWeaverHelpers.getAllPossibilitiesRootedGraph(pomDependencies, addedValuesToCompute);
            // Transform Json to JgraphT graph
            System.out.println(dtf.format(LocalDateTime.now())+" Graph transform");
            GraphGenerator graphGenerator = new JgraphtGraphGenerator();
            GraphStructure graph = graphGenerator.generateAllPossibilitiesRootedGraphFromJsonObject(jsonAllPossibilitiesRootedGraph, addedValuesToCompute);
            // Create change edges
            System.out.println(dtf.format(LocalDateTime.now())+" Graph size: "+graph.getVertexSet().size()+" vertices, "+graph.getEdgeSet().size()+"edges");
            System.out.println(dtf.format(LocalDateTime.now())+" Create change edge");
            graph.generateChangeEdge();
            System.out.println(dtf.format(LocalDateTime.now())+" Graph size: "+graph.getVertexSet().size()+" vertices, "+graph.getEdgeSet().size()+"edges");

            // compute direct dep cost
            System.out.println(dtf.format(LocalDateTime.now())+" Compute cost");
            Set<ArtifactNode> artifactDirectDeps = graph.getRootArtifactDirectDep();
            System.out.println(dtf.format(LocalDateTime.now())+" Size before: "+graph.getVertexSet().size());
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
            System.out.println(dtf.format(LocalDateTime.now())+" Size after: "+graph.getVertexSet().size());
            // Gurobi export

        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
    }
}
