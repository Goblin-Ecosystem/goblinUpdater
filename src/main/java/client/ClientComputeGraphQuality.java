package client;

import org.json.simple.JSONObject;
import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;
import updater.api.metrics.MetricNormalizer;
import updater.api.metrics.MetricType;
import updater.api.preferences.Preferences;
import updater.api.process.graphbased.RootedGraphGenerator;
import updater.api.project.Project;
import updater.api.project.ProjectLoader;
import updater.helpers.GoblinWeaverHelpers;
import updater.impl.graph.jgrapht.JgraphtRootedGraphGenerator;
import updater.impl.maven.project.MavenProjectLoader;
import updater.impl.metrics.MetricMaxValueNormalizer;
import updater.impl.preferences.SimplePreferences;
import util.helpers.system.LoggerHelpers;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public class ClientComputeGraphQuality {

    public static void main(String[] args) {
        LoggerHelpers.instance().setLevel(LoggerHelpers.Level.INFO);
        Path projectPath = Path.of(System.getProperty("projectPath"));
        Path preferencesPath = Path.of(System.getProperty("confFile"));
        ProjectLoader loader = new MavenProjectLoader();
        Optional<Project> optProject = loader.load(projectPath);
        if(optProject.isPresent()) {
            Project project = optProject.get();
            // Get graph
            Preferences updatePreferences = new SimplePreferences(preferencesPath);
            Set<MetricType> metricsToCompute = updatePreferences.qualityMetrics();
            JSONObject jsonRootedGraph = GoblinWeaverHelpers.getRootedGraph(project.getDirectDependencies(), metricsToCompute);
            RootedGraphGenerator jgraphtGraphGenerator = new JgraphtRootedGraphGenerator();
            UpdateGraph<UpdateNode, UpdateEdge> updateGraph = jgraphtGraphGenerator.generateRootedGraphFromJsonObject(jsonRootedGraph, metricsToCompute);
            LoggerHelpers.instance().info("Release nodes size: "+updateGraph.nodes(UpdateNode::isRelease).size());
            LoggerHelpers.instance().info("Artifact nodes size: "+updateGraph.nodes(UpdateNode::isArtifact).size());
            LoggerHelpers.instance().info("Dependency edges size: "+updateGraph.edges(UpdateEdge::isDependency).size());
            LoggerHelpers.instance().info("Version edges size: "+updateGraph.edges(UpdateEdge::isVersion).size());
            MetricNormalizer normalizer = new MetricMaxValueNormalizer();
            normalizer.normalize(updateGraph);
            logGraphQuality(updateGraph, updatePreferences);
        }
        else {
            LoggerHelpers.instance().error("Unable to get project");
        }
    }

    private static void logGraphQuality(UpdateGraph<UpdateNode, UpdateEdge> updateGraph, Preferences updatePreferences) {
        double totalQuality = 0.0;
        for(UpdateNode node : updateGraph.nodes(UpdateNode::isRelease).stream().filter(node -> !node.equals(updateGraph.rootNode().get())).toList()){
            totalQuality += node.getQuality(updatePreferences);
        }
        LoggerHelpers.instance().info("Initial graph quality: "+totalQuality);
    }
}
