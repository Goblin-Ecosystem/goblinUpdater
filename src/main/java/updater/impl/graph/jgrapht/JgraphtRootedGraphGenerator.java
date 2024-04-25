package updater.impl.graph.jgrapht;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
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
        generateChangeEdgeReleaseMode(projectPath, graph, updatePreferences);
        // generateChangeEdgeArtifactMode(projectPath, graph, updatePreferences);
        LoggerHelpers.instance().info("Change edges size: " + graph.edges(UpdateEdge::isChange).size());
        LoggerHelpers.instance().info("Compute change edge values");
        computeChangeEdgeValues(graph, projectPath, updatePreferences);
    }

    private Predicate<UpdateNode> hasSeveralVersions = n -> graph.versions(n).size() >= 2;

    private Set<UpdateNode> absenceLibraries(UpdateGraph<UpdateNode, UpdateEdge> graph, Preferences updatePreferences) {
        Function<Constraint<String>, Optional<UpdateNode>> absence = c -> (c instanceof AbsenceConstraint ac) ? graph.getNode(ac.focus()) : Optional.empty();
        return updatePreferences.constraints().stream()
            .flatMap(absence.andThen(Optional::stream))
            .filter(hasSeveralVersions)
            .collect(Collectors.toSet());
    }

    // depends on costs:focus:
    // NONE:        F={},                                                   CE = {f => r | f \in F, f-dep->l, l-ver->r} = {}
    // ALL:         F=N^R,                                                  CE = {f => r | f \in F, f-dep->l, l-ver->r}
    // CONSTRAINTS: F={f | r \in constraint focuses, l-ver->r, f-dep->l}    CE = {f => r | f \in F, f-dep->l, l-ver->r}
    // ROOT:        F={root},                                               CE = {f => r | f \in F, f-dep->l, l-ver->r}
    // default is ROOT.
    // optimization: use {f => r | f \in F, f-dep->l, |{r | l-ver->r}|>=2, l-ver->r}
    //        instead of {f => r | f \in F, f-dep->l, l-ver->r}.
    public void generateChangeEdgeReleaseMode(Path projectPath, UpdateGraph<UpdateNode, UpdateEdge> graph,
            Preferences updatePreferences) {
        Set<UpdateNode> focuses = switch (updatePreferences.changeFocus()) {
            case NONE -> Set.of();
            case ALL -> graph.releaseNodes();
            case CONSTRAINTS -> absenceLibraries(graph, updatePreferences).stream()
                                    .flatMap(l -> graph.directDependents(l).stream())
                                    .collect(Collectors.toSet());
            default -> graph.rootNode().map(Set::of).orElse(Set.of());
        };
        generateChangeEdgeWithReleaseFocuses(graph, focuses);
    }

    // depends on costs:focus:
    // NONE:        F={},                                       CE = {d => r | f \in F-, d-dep->f, l-ver->r} = {}
    // ALL:         F=N^L,                                      CE = {d => r | f \in F-, d-dep->f, l-ver->r}
    // CONSTRAINTS: F={l | r \in constraint focuses, l-ver->r}  CE = {d => r | f \in F-, d-dep->f, l-ver->r}
    // ROOT:        F={l | root-dep->l},                        CE = {d => r | f \in F-, d-dep->f, l-ver->r}
    // default is ROOT.
    // optimization: use F- = {l | l \in F, |{r | l-ver->r}| >= 2} instead of F.
    public void generateChangeEdgeArtifactMode(Path projectPath, UpdateGraph<UpdateNode, UpdateEdge> graph,
            Preferences updatePreferences) {
        Set<UpdateNode> focuses = switch (updatePreferences.changeFocus()) {
            case NONE -> Set.of();
            case ALL -> graph.nodes(((Predicate<UpdateNode>) (UpdateNode::isArtifact))
                    .and(hasSeveralVersions));
            case CONSTRAINTS -> absenceLibraries(graph, updatePreferences);
            default -> graph.rootDirectDependencies().stream()
                    .filter(hasSeveralVersions)
                    .collect(Collectors.toSet());
        };
        generateChangeEdgeWithArtifactFocuses(graph, focuses);
    }

    // a focus f is a release for which we compute change edges
    // it is done for all f -dep-> l -ver-> ri
    // TODO: is copy needed?
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
    // TODO: is copy needed?
    private void generateChangeEdgeWithArtifactFocuses(UpdateGraph<UpdateNode, UpdateEdge> graph,
            Set<UpdateNode> focuses) {
        UpdateGraph<UpdateNode, UpdateEdge> graphCopy = graph.copy();
        IdGenerator generator = IdGenerator.instance();
        focuses.forEach(f -> constructChangeEdgeWithArtifactFocus(generator, graph, graphCopy, f));
    }

    // focus is an artifact node
    // TODO:
    private void constructChangeEdgeWithArtifactFocus(IdGenerator generator, UpdateGraph<UpdateNode, UpdateEdge> graph,
            UpdateGraph<UpdateNode, UpdateEdge> graphCopy, UpdateNode focus) {
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
    // TODO: discuss integration of MARACAS and JAPICMP costs but for case with 0.0 limit
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
                            MaracasHelpers.computeChangeCost(projectPath, currentRelease.get(), releaseToCompute, preferences);
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
