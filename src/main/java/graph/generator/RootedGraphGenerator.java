package graph.generator;

import addedvalue.AddedValueEnum;
import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import graph.structures.UpdateGraph;
import updater.preferences.UpdatePreferences;

import org.json.simple.JSONObject;

import java.nio.file.Path;
import java.util.Set;

public interface RootedGraphGenerator {
    UpdateGraph<UpdateNode, UpdateEdge> generateRootedGraphFromJsonObject(JSONObject weaverJsonGraph, Set<AddedValueEnum> addedValuesToCompute);
    void generateChangeEdge(Path projectPath, UpdateGraph<UpdateNode, UpdateEdge> graph, UpdatePreferences updatePreferences);
}
