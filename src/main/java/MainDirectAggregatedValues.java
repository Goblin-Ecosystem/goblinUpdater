import addedvalue.AddedValueEnum;
import graph.entities.nodes.ArtifactNode;
import graph.entities.nodes.ReleaseNode;
import graph.generator.GraphGenerator;
import graph.generator.JgraphtGraphGenerator;
import graph.structures.GraphStructure;
import org.apache.maven.model.Dependency;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.json.simple.JSONObject;
import util.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainDirectAggregatedValues {

    // Direct dependencies all possibilities & aggregated metrics
    public static void mainDirectAggregatesValues(String[] args){
        String projectPath = System.getProperty("projectPath");
        try {
            // Get pom direct dependencies
            Set<Dependency> pomDependencies = MavenHelpers.getProjectDirectDependencies(projectPath);
            Set<AddedValueEnum> addedValuesToCompute = YmlConfReader.getInstance().getAddedValueEnumSet(); //TODO here metrics aggregated
            JSONObject jsonDirectPossibilitiesRootedGraph = GoblinWeaverHelpers.getDirectPossibilitiesRootedGraph(pomDependencies, addedValuesToCompute);
            // Transform Json to JgraphT graph
            GraphGenerator graphGenerator = new JgraphtGraphGenerator();
            GraphStructure graph = graphGenerator.generateRootedGraphFromJsonObject(jsonDirectPossibilitiesRootedGraph, addedValuesToCompute);
            // compute direct dep cost
            LoggerHelpers.info("Compute quality and cost");
            Set<ArtifactNode> artifactDirectDeps = graph.getRootArtifactDirectDep();
            for(ArtifactNode artifactDirectDep : artifactDirectDeps){
                // Get current used version
                ReleaseNode currentRelease = graph.getCurrentUseReleaseOfArtifact(artifactDirectDep);
                Set<ReleaseNode> allArtifactRelease = graph.getAllArtifactRelease(artifactDirectDep);
                double currentReleaseQuality = currentRelease.getNodeQuality();
                for(ReleaseNode artifactRelease : allArtifactRelease){
                    // If quality of current release < artifact release, delete node
                    if(currentReleaseQuality <= artifactRelease.getNodeQuality()){
                        graph.removeVertex(artifactRelease);
                    }
                    // Else compute change cost
                    else{
                        artifactRelease.setChangeCost(MaracasHelpers.computeChangeCost(projectPath, currentRelease, artifactRelease));
                    }
                }
                MavenLocalRepository.getInstance().clearLocalRepo();
                System.out.println("\n------------------");
                findOptimals(allArtifactRelease, currentRelease);
                System.out.println("------------------\n");
            }
        } catch (IOException | XmlPullParserException e) {
            LoggerHelpers.fatal(e.getMessage());
        }
    }

    private static void findOptimals(Set<ReleaseNode> allArtifactRelease, ReleaseNode currentRelease) {
        List<ReleaseNode> optimals = new ArrayList<>();

        for (ReleaseNode candidate : allArtifactRelease) {
            boolean isDominant = false;
            List<ReleaseNode> toDelete = new ArrayList<>();
            for (ReleaseNode current : optimals) {
                if (current.dominates(candidate)) {
                    isDominant = true;
                    break;
                } else if (candidate.dominates(current)) {
                    toDelete.add(current);
                }
            }
            optimals.removeAll(toDelete);

            if (!isDominant) {
                optimals.add(candidate);
            }
        }

        System.out.println("Optimals for change: "+currentRelease.getId());
        for (ReleaseNode opti : optimals){
            System.out.println("\t"+opti.getId() + " quality:"+opti.getNodeQuality()+" cost:"+opti.getChangeCost());
        }
    }
}
