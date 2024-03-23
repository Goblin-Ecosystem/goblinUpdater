package updater.impl.graph;

import updater.api.graph.UpdateGraph;
import updater.api.metrics.MetricType;
import updater.api.preferences.Preferences;
import updater.impl.graph.edges.UpdateEdge;
import updater.impl.graph.nodes.UpdateNode;

import org.json.simple.JSONObject;

import java.nio.file.Path;
import java.util.Set;

public interface RootedGraphGenerator {
    UpdateGraph<UpdateNode, UpdateEdge> generateRootedGraphFromJsonObject(JSONObject weaverJsonGraph, Set<MetricType> addedValuesToCompute);
    void generateChangeEdge(Path projectPath, UpdateGraph<UpdateNode, UpdateEdge> graph, Preferences updatePreferences);
}
