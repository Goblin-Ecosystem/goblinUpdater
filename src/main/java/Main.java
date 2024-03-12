import addedvalue.AddedValueEnum;
import graph.entities.edges.ChangeEdge;
import graph.entities.nodes.ArtifactNode;
import graph.entities.nodes.NodeType;
import graph.entities.nodes.ReleaseNode;
import graph.generator.GraphGenerator;
import graph.generator.JgraphtGraphGenerator;
import graph.structures.GraphStructure;
import org.apache.maven.model.Dependency;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.json.simple.JSONObject;
import util.GoblinWeaverHelpers;
import util.MaracasHelpers;
import util.MavenHelpers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    // Direct all possibilities & transitives dependencies
    public static void main(String[] args){
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
            System.out.println(dtf.format(LocalDateTime.now())+" Get direct all possibilities graph");
            JSONObject jsonDirectPossibilitiesRootedGraph = GoblinWeaverHelpers.getDirectPossibilitiesWithTransitiveRootedGraph(pomDependencies, addedValuesToCompute);
            // Transform Json to JgraphT graph
            System.out.println(dtf.format(LocalDateTime.now())+" Graph transform");
            GraphGenerator graphGenerator = new JgraphtGraphGenerator();
            GraphStructure graph = graphGenerator.generateAllPossibilitiesRootedGraphFromJsonObject(jsonDirectPossibilitiesRootedGraph, addedValuesToCompute);
            System.out.println(dtf.format(LocalDateTime.now())+" Graph size: "+graph.getVertexSet().size()+" vertices, "+graph.getEdgeSet().size()+"edges");
            System.out.println(dtf.format(LocalDateTime.now())+" Create change edge");
            graph.generateChangeEdge();
            System.out.println(dtf.format(LocalDateTime.now())+" Graph size: "+graph.getVertexSet().size()+" vertices, "+graph.getEdgeSet().size()+"edges");
            System.out.println(dtf.format(LocalDateTime.now())+" Compute change link values");
            // compute change link quality and cost
            for(ReleaseNode sourceReleaseNode : graph.getVertexSet().stream().filter(n -> n.getType().equals(NodeType.RELEASE)).map(ReleaseNode.class::cast).collect(Collectors.toSet())){
                double sourceReleaseNodeQuality = sourceReleaseNode.getNodeQuality();
                for(ChangeEdge changeEdge : graph.getChangeEdgeOf(sourceReleaseNode)){
                    ReleaseNode targetReleaseNode = (ReleaseNode) graph.getEdgeTarget(changeEdge);
                    changeEdge.setQualityChange(targetReleaseNode.getNodeQuality() - sourceReleaseNodeQuality);
                    // Compute cost only for direct dependencies
                    if(sourceReleaseNode.getId().equals("ROOT")){
                        changeEdge.setChangeCost(MaracasHelpers.computeChangeCost(projectPath, graph.getCurrentUseReleaseOfArtifact(new ArtifactNode(targetReleaseNode.getGa(), false)), targetReleaseNode));
                    }
                    else {
                        changeEdge.setChangeCost(9999999.9);
                    }
                }
            }
            System.out.println(dtf.format(LocalDateTime.now())+" End compute change link values");

        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
    }
}
