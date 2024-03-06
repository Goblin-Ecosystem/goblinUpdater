import addedvalue.AddedValueEnum;
import com.github.maracas.LibraryJar;
import com.github.maracas.Maracas;
import com.github.maracas.SourcesDirectory;
import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.BreakingChange;
import com.github.maracas.delta.Delta;
import graph.entities.nodes.ArtifactNode;
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
import util.MavenLocalRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Main {

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
            List<AddedValueEnum> addedValuesToCompute = List.of(AddedValueEnum.CVE_AGGREGATED, AddedValueEnum.FRESHNESS_AGGREGATED);
            System.out.println(dtf.format(LocalDateTime.now())+" Get direct all possibilities graph");
            JSONObject jsonDirectPossibilitiesRootedGraph = GoblinWeaverHelpers.getDirectPossibilitiesRootedGraph(pomDependencies, addedValuesToCompute);
            // Transform Json to JgraphT graph
            System.out.println(dtf.format(LocalDateTime.now())+" Graph transform");
            GraphGenerator graphGenerator = new JgraphtGraphGenerator();
            GraphStructure graph = graphGenerator.generateAllPossibilitiesRootedGraphFromJsonObject(jsonDirectPossibilitiesRootedGraph, addedValuesToCompute);
            System.out.println(dtf.format(LocalDateTime.now())+" Graph size: "+graph.getVertexSet().size()+" vertices, "+graph.getEdgeSet().size()+"edges");
            // compute direct dep cost
            System.out.println(dtf.format(LocalDateTime.now())+" Compute quality and cost");
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
            e.printStackTrace();
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
