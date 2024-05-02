package updater.impl.graph.jgrapht;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;
import updater.api.metrics.MetricType;
import updater.api.preferences.Constraint;
import updater.api.preferences.Preferences;
import updater.api.process.graphbased.RootedGraphGenerator;
import updater.helpers.JapicmpHelpers;
import updater.helpers.MaracasHelpers;
import updater.impl.graph.structure.edges.ChangeEdge;
import updater.impl.graph.structure.edges.DependencyEdge;
import updater.impl.graph.structure.edges.VersionEdge;
import updater.impl.graph.structure.nodes.AbstractNode;
import updater.impl.graph.structure.nodes.ArtifactNode;
import updater.impl.graph.structure.nodes.ReleaseNode;
import updater.impl.metrics.SimpleMetricDeclarator;
import updater.impl.metrics.SimpleMetricType;
import updater.impl.preferences.AbsenceConstraint;
import updater.impl.preferences.PresenceConstraint;
import util.IdGenerator;
import util.api.CustomGraph;
import util.helpers.system.LoggerHelpers;

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

    // TODO: Option to choose from the two modes
    @Override
    public void generateChangeEdge(Path projectPath, UpdateGraph<UpdateNode, UpdateEdge> graph,
            Preferences updatePreferences) {
        LoggerHelpers.instance().info("Generate change edges");
        generateChangeEdgeWork(projectPath, graph, updatePreferences);
        LoggerHelpers.instance().info("Change edges size: " + graph.edges(UpdateEdge::isChange).size());
        LoggerHelpers.instance().info("Compute change edge values");
        computeChangeEdgeValues(graph, projectPath, updatePreferences);
    }

    private final Predicate<UpdateNode> hasSeveralVersions = n -> graph.versions(n).size() >= 2;

    private Optional<String> getArtifactId(String id) {
        String [] parts = id.split(":");
        return switch (parts.length) {
            case 2 -> Optional.of(id);
            case 3 -> Optional.of(String.format("%s:%s", parts[0], parts[1]));
            default -> Optional.empty();
        };
    }

    private Optional<String> getReleaseId(String id) {
        String [] parts = id.split(":");
        return switch (parts.length) {
            case 3 -> Optional.of(id);
            default -> Optional.empty();
        };
    }

    private Optional<String> getLibraryFromConstraint(Constraint<String> constraint) {
        if (constraint instanceof AbsenceConstraint ac) {
            return getArtifactId(ac.value());
        } else if (constraint instanceof PresenceConstraint pc) {
            return getArtifactId(pc.value());
        } else {
            return Optional.empty();
        }

    }

    private Set<UpdateNode> constrainedLibraries(UpdateGraph<UpdateNode, UpdateEdge> graph, Preferences updatePreferences) {
        return updatePreferences.constraints().stream()
                .map(this::getLibraryFromConstraint)
                .filter(Optional::isPresent).map(Optional::get)
                .map(graph::getNode)
                .filter(Optional::isPresent).map(Optional::get)
                .filter(hasSeveralVersions)
                .collect(Collectors.toSet());
    }

    public void generateChangeEdgeWork(Path projectPath, UpdateGraph<UpdateNode, UpdateEdge> graph,
            Preferences updatePreferences) {
        switch (updatePreferences.changeFocus()) {
            case NONE:
                generateChangeEdgeWithReleaseFocuses(graph, Set.of());
                break;
            case GLOBAL:
                generateChangeEdgeWithReleaseFocuses(graph, graph.releaseNodes());
                break;
            case LOCAL:
                generateChangeEdgeWithReleaseFocuses(graph, graph.rootNode().map(Set::of).orElse(Set.of()));
                break;
                case CONSTRAINTS:
                generateChangeEdgeWithArtifactFocuses(graph, constrainedLibraries(graph, updatePreferences));
                break;
            case LOCAL_AND_CONSTRAINTS:
                generateChangeEdgeWithReleaseFocuses(graph, graph.rootNode().map(Set::of).orElse(Set.of()));
                generateChangeEdgeWithArtifactFocuses(graph, constrainedLibraries(graph, updatePreferences));
                break;
        }
    }

    // a focus f is a release for which we compute change edges
    // it is done for all f -dep-> l -ver-> ri
    // TODO: copy is needed (by now) but costly!
    private void generateChangeEdgeWithReleaseFocuses(UpdateGraph<UpdateNode, UpdateEdge> graph,
            Set<UpdateNode> focuses) {
        UpdateGraph<UpdateNode, UpdateEdge> graphCopy = graph.copy();
        IdGenerator generator = IdGenerator.instance();
        focuses.forEach(f -> constructChangeEdgeWithReleaseFocus(generator, graph, graphCopy, f));
    }

    // focus is a release node
    // {f => r | f \in F, f-dep->l, |{r | l-ver->r}|>=2, l-ver->r}
    private void constructChangeEdgeWithReleaseFocus(IdGenerator generator, UpdateGraph<UpdateNode, UpdateEdge> graph,
            UpdateGraph<UpdateNode, UpdateEdge> graphCopy, UpdateNode focus) {
        graphCopy.outgoingEdgesOf(focus).stream()
                .filter(UpdateEdge::isDependency)
                .map(edge -> graphCopy.versions(graphCopy.target(edge)))
                .filter(rs -> rs.size() >= 2)
                .flatMap(Set::stream)
                .forEach(r -> graph.addEdgeFromNodeId(focus.id(), r.id(),
                        new ChangeEdge(generator.nextId(EDGE_PREFIX), Map.of())));
    }

    // a focus f is an artifact for which we compute change edges
    // it is done for all di -dep-> f -ver-> rj
    // TODO: copy is needed (by now) but costly!
    private void generateChangeEdgeWithArtifactFocuses(UpdateGraph<UpdateNode, UpdateEdge> graph,
            Set<UpdateNode> focuses) {
        UpdateGraph<UpdateNode, UpdateEdge> graphCopy = graph.copy();
        IdGenerator generator = IdGenerator.instance();
        focuses.forEach(f -> constructChangeEdgeWithArtifactFocus(generator, graph, graphCopy, f));
    }

    // focus is an artifact node
    // {d => r | f \in F-, d-dep->f, f-ver->r}
    private void constructChangeEdgeWithArtifactFocus(IdGenerator generator, UpdateGraph<UpdateNode, UpdateEdge> graph,
            UpdateGraph<UpdateNode, UpdateEdge> graphCopy, UpdateNode focus) {
        Set<UpdateNode> dependents = graphCopy.directDependents(focus);
        Set<UpdateNode> versions = graphCopy.versions(focus);
        dependents.forEach(d -> versions.forEach(
                r -> graph.addEdgeFromNodeId(d.id(), r.id(), new ChangeEdge(generator.nextId(EDGE_PREFIX), Map.of()))));
    }

    // we compute the cost for a change edge (source, releaseToCompute)
    // given source -dep-> artifactOfReleaseToCompute -ver-> releaseToCompute
    // (compared)
    // and source -dep-> artifactOfReleaseToCompute -ver-> currentRelease (used)
    // if problem: cost = default
    // if source is ROOT:
    // - if tool-direct is NONE: cost = default
    // - if tool-direct is MARACAS: cost = computed using Maracas
    // if source is not ROOT:
    // - if tool-indirect is NONE: cost = default
    // - if tool-indirect is JAPICMP: cost = computed using Japicmp
    // TODO: discuss integration of MARACAS and JAPICMP costs but for case with 0.0
    // limit
    private void computeChangeEdgeValues(UpdateGraph<UpdateNode, UpdateEdge> graph, Path projectPath,
            Preferences preferences) {
        graph.changeEdges().forEach(changeEdge -> {
            UpdateNode source = graph.source(changeEdge);
            UpdateNode releaseToCompute = graph.target(changeEdge);
            Optional<UpdateNode> artifactOfReleaseToCompute = graph.artifactOf(releaseToCompute);
            Optional<UpdateNode> currentRelease = artifactOfReleaseToCompute
                    .flatMap(a -> graph.currentDependencyRelease(source, a));
            double cost;
            if (!currentRelease.isPresent()) {
                LoggerHelpers.instance().error(String.format("Unable to find current used release for change %s -> %s",
                        source.id(), releaseToCompute.id()));
                cost = preferences.defaultCost().toDouble();
            } else {
                boolean sourceIsRoot = source.id().equals(CustomGraph.ROOT_ID);
                if (sourceIsRoot) {
                    cost = switch (preferences.directTool()) {
                        case NONE -> preferences.defaultCost().toDouble();
                        case MARACAS ->
                            MaracasHelpers.computeChangeCost(projectPath, currentRelease.get(), releaseToCompute,
                                    preferences);
                    };
                } else {
                    cost = switch (preferences.indirectTool()) {
                        case NONE -> preferences.defaultCost().toDouble();
                        case JAPICMP ->
                            JapicmpHelpers.computeChangeCost(currentRelease.get(), releaseToCompute);
                    };
                }
            }
            changeEdge.put(SimpleMetricType.COST, cost);
        });
    }
}
