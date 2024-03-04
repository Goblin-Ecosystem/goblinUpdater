package graph.generator;

import addedvalue.AddedValueEnum;
import graph.structures.GraphStructure;
import org.json.simple.JSONObject;

import java.util.List;

public interface GraphGenerator {
    GraphStructure generateAllPossibilitiesRootedGraphFromJsonObject(JSONObject jsonAllPossibilitiesRootedGraph, List<AddedValueEnum> addedValuesToCompute);
}
