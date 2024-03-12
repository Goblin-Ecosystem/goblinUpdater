package graph.generator;

import addedvalue.AddedValueEnum;
import graph.structures.GraphStructure;
import org.json.simple.JSONObject;

import java.util.Set;

public interface GraphGenerator {
    GraphStructure generateRootedGraphFromJsonObject(JSONObject jsonAllPossibilitiesRootedGraph, Set<AddedValueEnum> addedValuesToCompute);
}
