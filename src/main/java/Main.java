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
import util.LoggerHelpers;
import util.MaracasHelpers;
import util.MavenHelpers;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
            graph.generateChangeEdge();
            LoggerHelpers.info("Compute change link values");
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
        } catch (IOException | XmlPullParserException e) {
            LoggerHelpers.fatal(e.getMessage());
        }
    }
}
