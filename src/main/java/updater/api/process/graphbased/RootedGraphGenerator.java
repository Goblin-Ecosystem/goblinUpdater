package updater.api.process.graphbased;

import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;
import updater.api.metrics.MetricType;
import updater.api.preferences.Preferences;

import org.json.simple.JSONObject;

import java.nio.file.Path;
import java.util.Set;

public interface RootedGraphGenerator {
    UpdateGraph<UpdateNode, UpdateEdge> generateRootedGraphFromJsonObject(JSONObject weaverJsonGraph, Set<MetricType> metricsToCompute);
    void generateChangeEdge(Path projectPath, UpdateGraph<UpdateNode, UpdateEdge> graph, Preferences updatePreferences);
}
