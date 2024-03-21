package graph.generator;

import addedvalue.AddedValueEnum;
import graph.entities.edges.*;
import graph.entities.nodes.*;
import graph.structures.CustomGraph;
import graph.structures.UpdateGraph;
import graph.structures.jgrapht.JgraphtUpdateGraph;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import updater.preferences.UpdatePreferences;

import java.util.Optional;
import java.util.HashSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import util.LoggerHelpers;
import util.MaracasHelpers;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JgraphtRootedGraphGenerator implements RootedGraphGenerator {

    static final String EDGE_PREFIX = "e";

    private UpdateGraph<UpdateNode, UpdateEdge> graph;

    public JgraphtRootedGraphGenerator() {
        graph = null;
    }

    private static final Optional<String> stringFromJSON(JSONObject object, String field) {
        return Optional.ofNullable(object.get(field)).map(s -> (String) s);
    }

    private Optional<UpdateEdge> extractEdge(JSONObject edge) {
        Optional<String> type = stringFromJSON(edge, "type");
        if (type.isPresent()) {
            return switch (type.get()) {
                case "DEPENDENCY" -> extractDependencyEdge(edge);
                case "RELATIONSHIP_AR" -> extractVersionEdge();
                case "CHANGE" -> extractChangeEdge();
                default -> Optional.empty();
            };
        } else {
            return Optional.empty();
        }
    }

    private Optional<UpdateEdge> extractVersionEdge() {
        return Optional.of(new VersionEdge(IdGenerator.instance().nextId(EDGE_PREFIX), Map.of()));
    }

    private Optional<UpdateEdge> extractChangeEdge() {
        return Optional.of(new ChangeEdge(IdGenerator.instance().nextId(EDGE_PREFIX), Map.of()));
    }

    private Optional<UpdateEdge> extractDependencyEdge(JSONObject edge) {
        Optional<String> targetVersion = stringFromJSON(edge, "targetVersion");
        Optional<String> scope = stringFromJSON(edge, "scope");
        if (targetVersion.isPresent() && scope.isPresent()) {
            return Optional
                    .of(new DependencyEdge(IdGenerator.instance().nextId(EDGE_PREFIX), targetVersion.get(),
                            scope.get(), Map.of()));
        } else {
            return Optional.empty();
        }
    }

    // FIXME: add added values here
    private final Consumer<JSONObject> createEdge = edge -> {
        Optional<String> sourceId = stringFromJSON(edge, "sourceId");
        Optional<String> targetId = stringFromJSON(edge, "targetId");
        if (sourceId.isPresent() && targetId.isPresent()) {
            extractEdge(edge).ifPresent(e -> {
                synchronized (graph) {
                    graph.addEdgeFromNodeId(sourceId.get(), targetId.get(), e);
                }
            });
        }
    };

    private final Function<Set<AddedValueEnum>, Consumer<JSONObject>> createNode = avs -> node -> extractNode(node)
            .ifPresent(n -> {
                Set<AddedValueEnum> effectiveAvs = new HashSet<>(avs);
                effectiveAvs.retainAll(n.knownValues());
                addValues(n, node, effectiveAvs);
                synchronized (graph) {
                    graph.addNode(n);
                }
            });

    // FIXME: add added values here
    private Optional<AbstractNode> extractNode(JSONObject node) {
        Optional<String> oType = stringFromJSON(node, "nodeType");
        Optional<String> oId = stringFromJSON(node, "id");
        if (oId.isPresent() && oType.isPresent()) {
            String id = oId.get();
            String type = oType.get();
            return Optional.ofNullable(switch (type) {
                case "ARTIFACT" -> new ArtifactNode(id, Map.of());
                case "RELEASE" -> new ReleaseNode(id, Map.of());
                default -> null;
            });
        } else {
            return Optional.empty();
        }
    }

    private void addValues(AbstractNode n, JSONObject node, Set<AddedValueEnum> avs) {
        for (AddedValueEnum av : avs) {
            try {
                n.addAddedValue(av.getAddedValueClass()
                        .getDeclaredConstructor(JSONObject.class)
                        .newInstance(node));
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                LoggerHelpers.warning(e.getMessage());
            }
        }
    }

    @Override
    public UpdateGraph<UpdateNode, UpdateEdge> generateRootedGraphFromJsonObject(JSONObject weaverJsonGraph,
            Set<AddedValueEnum> addedValuesToCompute) {
        graph = new JgraphtUpdateGraph();
        // Add elements in graph
        List<Tuple2<String, Consumer<JSONObject>>> creators = List.of(
                Tuple.of("nodes", createNode.apply(addedValuesToCompute)),
                Tuple.of("edges", createEdge));
        creators.stream().forEach(t -> {
            Object maybes = weaverJsonGraph.get(t._1());
            if ((maybes != null) && (maybes instanceof JSONArray os)) {
                os.parallelStream()
                        .filter(JSONObject.class::isInstance)
                        .forEach(n -> t._2().accept((JSONObject) n));
            }
        });
        // Log
        LoggerHelpers.info(graph.toString());
        return graph;
    }

    // FIXME: add added values here
    @Override
    public void generateChangeEdge(Path projectPath, UpdateGraph<UpdateNode, UpdateEdge> graph,
            UpdatePreferences updatePreferences) {
        // prepare things
        UpdateGraph<UpdateNode, UpdateEdge> graphCopy = graph.copy();
        IdGenerator generator = IdGenerator.instance();
        Set<ReleaseNode> releaseNodes = graphCopy.releaseNodes().stream()
                .map(ReleaseNode.class::cast).collect(Collectors.toSet());
        // step 1 : create change edges
        LoggerHelpers.info("Generate change edge");
        releaseNodes.forEach(
                r -> graphCopy.directDependencies(r).forEach(
                        a -> graphCopy.versions(a).forEach(
                                v -> graph.addEdgeFromNodeId(
                                        r.id(),
                                        v.id(),
                                        new ChangeEdge(generator.nextId(EDGE_PREFIX), Map.of())))));
        LoggerHelpers.info(graph.toString());
        // step 2: compute change edge quality and cost
        LoggerHelpers.info("Compute change edge values");
        releaseNodes.forEach(
                r -> {
                    graph.possibles(r).forEach(
                            e -> {
                                ChangeEdge change = (ChangeEdge) e;
                                ReleaseNode v = (ReleaseNode) graph.target(e);
                                // compute quality change in any case
                                change.setQualityChange(
                                        v.getNodeQuality(updatePreferences) - r.getNodeQuality(updatePreferences));
                                // compute cost only for direct dependencies of root
                                if (r.id().equals(CustomGraph.ROOT_ID)) {
                                    Optional<UpdateNode> or = graph.rootCurrentDependencyRelease(v);
                                    if (or.isPresent()) {
                                        change.setChangeCost(MaracasHelpers.computeChangeCost(
                                                projectPath,
                                                or.get(),
                                                v));
                                    } else {
                                        change.setChangeCost(9999999.9); // FIXME: OK ?
                                    }
                                } else {
                                    change.setChangeCost(9999999.9);
                                }
                            });
                });
    }
}
