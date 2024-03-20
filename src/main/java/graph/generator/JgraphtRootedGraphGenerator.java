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
import java.util.stream.Collectors;

public class JgraphtRootedGraphGenerator implements RootedGraphGenerator {

    final String EDGE_PREFIX = "e";

    private UpdateGraph<UpdateNode, UpdateEdge> graph;

    public JgraphtRootedGraphGenerator() {
        graph = null;
    }

    private static final Optional<String> stringFromJSON(JSONObject object, String field) {
        return Optional.ofNullable(object.get(field)).map(s -> (String) s);
    }

    private Optional<AbstractEdge> extractEdge(JSONObject edge) {
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

    private Optional<AbstractEdge> extractVersionEdge() {
        return Optional.of(new VersionEdge(IdGenerator.instance().nextId(EDGE_PREFIX)));
    }

    private Optional<AbstractEdge> extractChangeEdge() {
        return Optional.of(new ChangeEdge(IdGenerator.instance().nextId(EDGE_PREFIX)));
    }

    private Optional<AbstractEdge> extractDependencyEdge(JSONObject edge) {
        Optional<String> targetVersion = stringFromJSON(edge, "targetVersion");
        Optional<String> scope = stringFromJSON(edge, "scope");
        if (targetVersion.isPresent() && scope.isPresent()) {
            return Optional
                    .of(new DependencyEdge(IdGenerator.instance().nextId(EDGE_PREFIX), targetVersion.get(),
                            scope.get()));
        } else {
            return Optional.empty();
        }
    }

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

    private Optional<AbstractNode> extractNode(JSONObject node) {
        Optional<String> oType = stringFromJSON(node, "nodeType");
        Optional<String> oId = stringFromJSON(node, "id");
        if (oId.isPresent() && oType.isPresent()) {
            String id = oId.get();
            String type = oType.get();
            return Optional.ofNullable(switch (type) {
                case "ARTIFACT" -> new ArtifactNode(id);
                case "RELEASE" -> new ReleaseNode(id);
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

    @Override
    public void generateChangeEdge(Path projectPath, UpdateGraph<UpdateNode, UpdateEdge> graph,
            UpdatePreferences updatePreferences) {
        LoggerHelpers.info("Generate change edge");
        CustomGraph<UpdateNode, UpdateEdge> graphCopy = graph.copy();
        IdGenerator generator = IdGenerator.instance();
        graphCopy.nodes().stream()
                .filter(ReleaseNode.class::isInstance)
                .map(ReleaseNode.class::cast)
                .forEach(releaseNode -> graphCopy.outgoingEdgesOf(releaseNode).stream()
                        .filter(UpdateEdge::isDependency)
                        .map(graphCopy::target)
                        .forEach(artifactDependency -> graphCopy.outgoingEdgesOf(artifactDependency).stream()
                                .filter(UpdateEdge::isVersion)
                                .map(graphCopy::target)
                                .forEach(possibleRelease -> graph.addEdgeFromNodeId(releaseNode.id(),
                                        possibleRelease.id(), new ChangeEdge(generator.nextId(EDGE_PREFIX))))));
        LoggerHelpers.info(graph.toString());
        LoggerHelpers.info("Compute change edge values");
        // compute change link quality and cost
        for (ReleaseNode sourceReleaseNode : graph.nodes().stream().filter(UpdateNode::isRelease)
                .map(ReleaseNode.class::cast).collect(Collectors.toSet())) {
            double sourceReleaseNodeQuality = sourceReleaseNode.getNodeQuality(updatePreferences);
            for (UpdateEdge changeEdge : graph.getPossibleEdgesOf(sourceReleaseNode)) {
                ChangeEdge possibleEdge = (ChangeEdge) changeEdge;
                ReleaseNode targetReleaseNode = (ReleaseNode) graph.target(possibleEdge);
                possibleEdge.setQualityChange(
                        targetReleaseNode.getNodeQuality(updatePreferences) - sourceReleaseNodeQuality);
                // Compute cost only for direct dependencies
                if (sourceReleaseNode.id().equals("ROOT")) {
                    possibleEdge.setChangeCost(MaracasHelpers.computeChangeCost(projectPath,
                            graph.getCurrentUseReleaseOfArtifact(new ArtifactNode(targetReleaseNode.getGa())),
                            targetReleaseNode));
                } else {
                    possibleEdge.setChangeCost(9999999.9);
                }
            }
        }
    }
}
