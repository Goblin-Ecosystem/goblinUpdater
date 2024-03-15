package graph.generator;

import addedvalue.AddedValueEnum;
import graph.structures.UpdateGraph;
import bazarRefonte.UpdatePreferences;
import org.json.simple.JSONObject;
import project.Project;
import util.GoblinWeaverHelpers;
import util.YmlConfReader;

import java.util.Set;

public class LPGAGraphGenerator implements GraphGenerator {
    @Override
    public UpdateGraph computeUpdateGraph(Project project, UpdatePreferences updatePreferences) {
        //TODO use Maven Preferences
        Set<AddedValueEnum> addedValuesToCompute = YmlConfReader.getInstance().getAddedValueEnumSet();
        JSONObject jsonDirectPossibilitiesRootedGraph = GoblinWeaverHelpers.getDirectPossibilitiesWithTransitiveRootedGraph(project.getDirectDependencies(), addedValuesToCompute);
        return JgraphtGraphGenerator.generateRootedGraphFromJsonObject(jsonDirectPossibilitiesRootedGraph, addedValuesToCompute);
    }
}
