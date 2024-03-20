package graph.generator;

import addedvalue.AddedValueEnum;
import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import graph.structures.UpdateGraph;
import org.json.simple.JSONObject;
import updater.updatePreferences.UpdatePreferences;

import java.nio.file.Path;
import java.util.Set;
import java.util.Map;

public interface RootedGraphGenerator {
    UpdateGraph generateRootedGraphFromJsonObject(JSONObject weaverJsonGraph, Set<AddedValueEnum> addedValuesToCompute);
    void generateChangeEdge(Path projectPath, UpdateGraph<UpdateNode, UpdateEdge> graph, UpdatePreferences updatePreferences);
}
