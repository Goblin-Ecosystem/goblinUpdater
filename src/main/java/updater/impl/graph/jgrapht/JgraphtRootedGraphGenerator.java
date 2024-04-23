package updater.impl.graph.jgrapht;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;
import updater.api.metrics.MetricType;
import updater.api.preferences.Preferences;
import updater.api.process.graphbased.RootedGraphGenerator;
import updater.helpers.MaracasHelpers;
import updater.impl.graph.structure.edges.*;
import updater.impl.graph.structure.nodes.*;
import updater.impl.metrics.SimpleMetricDeclarator;

import java.util.Optional;
import java.util.HashSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import updater.impl.metrics.SimpleMetricType;
import util.IdGenerator;
import util.api.CustomGraph;
import util.helpers.system.LoggerHelpers;

import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.List;
import java.util.Map;

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

    private final Function<Set<MetricType>, Consumer<JSONObject>> createNode = avs -> node -> extractNode(node)
            .ifPresent(n -> {
                Set<MetricType> effectiveAvs = new HashSet<>(avs);
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

    private void addValues(AbstractNode n, JSONObject node, Set<MetricType> avs) {
        for (MetricType av : avs) {
            try {
                n.put(av, SimpleMetricDeclarator.instance().metric(av)
                        .orElseThrow(() -> new RuntimeException("No metric for " + av))
                        .getDeclaredConstructor(JSONObject.class)
                        .newInstance(node).compute());
            } catch (Exception e) {
                LoggerHelpers.instance().warning(e.getMessage());
            }
        }
    }

    @Override
    public UpdateGraph<UpdateNode, UpdateEdge> generateRootedGraphFromJsonObject(JSONObject weaverJsonGraph,
            Set<MetricType> metricsToCompute) {
        graph = new JgraphtUpdateGraph();
        // Add elements in graph
        List<Tuple2<String, Consumer<JSONObject>>> creators = List.of(
                Tuple.of("nodes", createNode.apply(metricsToCompute)),
                Tuple.of("edges", createEdge));
        creators.stream().forEach(t -> {
            Object maybes = weaverJsonGraph.get(t._1());
            if ((maybes != null) && (maybes instanceof JSONArray os)) {
                // os.parallelStream()
                os.stream()
                        .filter(JSONObject.class::isInstance)
                        .forEach(n -> t._2().accept((JSONObject) n));
            }
        });
        // Log
        LoggerHelpers.instance().info(graph.toString());
        return graph;
    }

    // FIXME: deal with preferences
    // costs.focuses in NONE, ROOT, (CONSTRAINTS), ALL
    // costs.default is a double
    // costs.tool-direct either (NONE) or MARACAS
    // costs.tool-indirect either NONE or (JAPICMP)
    @Override
    public void generateChangeEdge(Path projectPath, UpdateGraph<UpdateNode, UpdateEdge> graph,
            Preferences updatePreferences) {
        Set<UpdateNode> focuses = switch (updatePreferences.changeFocus()) {
            case NONE -> Set.of();
            case ALL -> graph.releaseNodes();
            default -> graph.rootNode().map(Set::of).orElse(Set.of());
        };
        generateChangeEdgeWithFocuses(projectPath, graph, focuses, updatePreferences);
    }

    // a focus f is a release for which we compute change edges
    // it is done for all f -dep-> l -ver-> ri
    private void generateChangeEdgeWithFocuses(Path projectPath, UpdateGraph<UpdateNode, UpdateEdge> graph, Set<UpdateNode> focuses, Preferences preferences) {
        // prepare things
        UpdateGraph<UpdateNode, UpdateEdge> graphCopy = graph.copy();
        IdGenerator generator = IdGenerator.instance();
        // step 1 : create change edges between focuses and direct dependencies
        LoggerHelpers.instance().info("Generate change edges");
        focuses.forEach(f -> constructChangeEdgeWithFocus(generator, graph, graphCopy, f));
        LoggerHelpers.instance().info("Change edges size: " + graph.edges(UpdateEdge::isChange).size());
        // step 2: compute change edge costs
        LoggerHelpers.instance().info("Compute change edge values");
        computeChangeEdgeValues(graph, projectPath, preferences);
    }

    private void constructChangeEdgeWithFocus(IdGenerator generator, UpdateGraph<UpdateNode, UpdateEdge> graph, UpdateGraph<UpdateNode, UpdateEdge> graphCopy, UpdateNode focus) {
        graphCopy.outgoingEdgesOf(focus).stream()
            .filter(UpdateEdge::isDependency)
            .forEach(edge -> graphCopy.versions(graphCopy.target(edge))
                .forEach(release -> graph.addEdgeFromNodeId(focus.id(), release.id(),
                        new ChangeEdge(generator.nextId(EDGE_PREFIX), Map.of()))));
    }

    // we compute the cost for a change edge (source, releaseToCompute)
    // given source -dep-> artifactOfReleaseToCompute -ver-> releaseToCompute (compared)
    // and   source -dep-> artifactOfReleaseToCompute -ver-> currentRelease (used)
    // if problem: cost = default
    // if source is ROOT:
    //  - if tool-direct is NONE: cost = default
    //  - if tool-direct is MARACAS: cost = computed using Maracas
    // if source is not ROOT:
    //  - if tool-indirect is NONE: cost = default
    //  - if tool-indirect is JAPICMP: cost = computed using Japicmp
    private void computeChangeEdgeValues(UpdateGraph<UpdateNode, UpdateEdge> graph, Path projectPath, Preferences preferences) {
        graph.changeEdges().forEach(changeEdge -> {
            UpdateNode source = graph.source(changeEdge);
            UpdateNode releaseToCompute = graph.target(changeEdge);
            Optional<UpdateNode> artifactOfReleaseToCompute = graph.artifactOf(releaseToCompute);
            Optional<UpdateNode> currentRelease = artifactOfReleaseToCompute.flatMap(a -> graph.currentDependencyRelease(source, a));
            double cost;
            if (!currentRelease.isPresent()) {
                LoggerHelpers.instance().error(String.format("Unable to find current used release for change %s -> %s", source.id(), releaseToCompute.id()));
                cost = preferences.defaultCost().toDouble();
            } else {
                boolean sourceIsRoot = source.id().equals(CustomGraph.ROOT_ID);
                if (sourceIsRoot) {
                    cost = switch (preferences.directTool()) {
                        case NONE -> preferences.defaultCost().toDouble();
                        case MARACAS -> MaracasHelpers.computeChangeCost(projectPath, currentRelease.get(), releaseToCompute);
                    };
                } else {
                    cost = switch (preferences.indirectTool()) {
                        case NONE -> preferences.defaultCost().toDouble();
                        case JAPICMP -> preferences.defaultCost().toDouble(); // TODO: add japicmp helper
                    };
                }
            }
            changeEdge.put(SimpleMetricType.COST, cost);
        });
    }
}
