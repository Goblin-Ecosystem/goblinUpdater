package graph.structures.mocks;

import java.util.List;
import java.util.Set;

import addedvalue.AddedValueEnum;
import static addedvalue.AddedValueEnum.*;
import addedvalue.MetricContainer;
import addedvalue.MetricMap;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import graph.entities.nodes.UpdateNode;
import graph.entities.edges.UpdateEdge;
import graph.structures.UpdateGraph;
import io.vavr.Tuple;
import io.vavr.Tuple2;

import static addedvalue.AddedValueEnum.CVE;
import static addedvalue.AddedValueEnum.FRESHNESS;
import static graph.structures.mocks.GraphMock001.EdgeType.*;
import static graph.structures.mocks.GraphMock001.NodeType.*;

import java.util.Optional;

public class GraphMock001 implements UpdateGraph<UpdateNode, UpdateEdge> {

    public enum NodeType {
        RELEASE, ARTIFACT;
    }

    public enum EdgeType {
        VERSION, DEPENDENCY, CHANGE;
    }

    public record Node001(String id, NodeType type, MetricContainer<AddedValueEnum> metrics) implements UpdateNode {

        public Node001(String id, NodeType type, MetricContainer<AddedValueEnum> metrics) {
            this.id = id;
            this.type = type;
            this.metrics = metrics;
            if (!hasValidId(id))
                throw new IllegalArgumentException("Invalid node id " + id);
        }

        @Override
        public Set<AddedValueEnum> usedMetrics() {
            return metrics.usedMetrics();
        }

        @Override
        public void addMetric(AddedValueEnum m, Double value) {
            metrics.addMetric(m, value);
        }

        @Override
        public Optional<Double> getValue(AddedValueEnum m) {
            return metrics.getValue(m);
        }

        @Override
        public boolean hasValidId(String id) {
            return id.split(":").length == switch (type) {
                case RELEASE -> 3;
                case ARTIFACT -> 2;
                default -> 0;
            };
        }

        @Override
        public boolean isRelease() {
            return type == RELEASE;
        }

        @Override
        public boolean isArtifact() {
            return type == ARTIFACT;
        }

        @Override
        public Set<AddedValueEnum> knownValues() {
            return switch (type) {
                case RELEASE -> Set.of(CVE, FRESHNESS);
                case ARTIFACT -> new HashSet<>();
                default -> Set.of();
            };
        }

    }

    public record Edge001(String id, EdgeType type, UpdateNode source, UpdateNode target, String targetVersion,
            MetricContainer<AddedValueEnum> metrics)
            implements UpdateEdge {

        @Override
        public Set<AddedValueEnum> usedMetrics() {
            return metrics.usedMetrics();
        }

        @Override
        public void addMetric(AddedValueEnum m, Double value) {
            metrics.addMetric(m, value);
        }

        @Override
        public Optional<Double> getValue(AddedValueEnum m) {
            return metrics.getValue(m);
        }

        @Override
        public boolean isVersion() {
            return type == VERSION;
        }

        @Override
        public boolean isDependency() {
            return type == DEPENDENCY;
        }

        @Override
        public boolean isChange() {
            return type == CHANGE;
        }

    }

    private Set<UpdateNode> nodes;
    private Set<UpdateEdge> edges;
    private UpdateNode root;

    private UpdateNode addNode(String id, NodeType type, MetricContainer<AddedValueEnum> metrics) {
        if (metrics == null) {
            metrics = new MetricMap<>(Map.of());
        }
        UpdateNode node = new Node001(id, type, metrics);
        addNode(node);
        return node;
    }

    @Override
    public void addNode(UpdateNode node) {
        nodes.add(node);
    }

    private UpdateEdge addEdge(String id, EdgeType type, String source, String target, String targetVersion,
            MetricContainer<AddedValueEnum> metrics) {
        Optional<UpdateNode> ns = getNodeById(source);
        Optional<UpdateNode> nt = getNodeById(target);
        if (ns.isEmpty())
            throw new IllegalArgumentException("source node not found");
        if (nt.isEmpty())
            throw new IllegalArgumentException("target node not found");
        if (metrics == null) {
            metrics = new MetricMap<>(Map.of());
        }
        UpdateEdge edge = new Edge001(id, type, ns.get(), nt.get(), targetVersion, metrics);
        addEdgeFromNodeId(ns.get().id(), nt.get().id(), edge);
        return edge;
    }

    @Override
    public void addEdgeFromNodeId(String fromId, String toId, UpdateEdge edge) {
        edges.add(edge);
    }

    private void setRoot(UpdateNode root) {
        this.root = root;
    }

    private GraphMock001() {
        nodes = new HashSet<>();
        edges = new HashSet<>();
        root = null;
    }

    @Override
    public Set<UpdateNode> nodes() {
        return nodes;
    }

    @Override
    public Set<UpdateEdge> edges() {
        return edges;
    }

    private Optional<UpdateNode> getNodeById(String id) {
        return nodes.stream().filter(n -> n.id().equals(id)).findFirst();
    }

    @Override
    public UpdateNode source(UpdateEdge e) {
        return ((Edge001) e).source(); // FIXME: wrong
    }

    @Override
    public UpdateNode target(UpdateEdge e) {
        return ((Edge001) e).target(); // FIXME: wrong
    }

    @Override
    public Optional<UpdateNode> rootNode() {
        return Optional.ofNullable(root);
    }

    private static final UpdateGraph<UpdateNode, UpdateEdge> generateGraph(String root, List<String> artifacts,
            List<String> releases, Map<String, List<String>> versions,
            Map<String, List<Tuple2<String, String>>> dependencies,
            Map<String, MetricContainer<AddedValueEnum>> metrics) {
        GraphMock001 graph = new GraphMock001();
        int idEdge = 0;
        // root (has no metrics)
        UpdateNode rootNode = graph.addNode(root, RELEASE, metrics.get(root));
        graph.setRoot(rootNode);
        // other releases
        for (String r : releases) {
            graph.addNode(r, RELEASE, metrics.get(r));
        }
        // artifacts
        for (String a : artifacts) {
            graph.addNode(a, ARTIFACT, metrics.get(a));
        }
        // versions
        for (Entry<String, List<String>> e : versions.entrySet()) {
            for (String v : e.getValue()) {
                graph.addEdge("e" + idEdge++, VERSION, e.getKey(), v, null, null);
            }
        }
        // dependencies and possibles
        for (Entry<String, List<Tuple2<String, String>>> e : dependencies.entrySet()) {
            for (Tuple2<String, String> d : e.getValue()) {
                graph.addEdge("e" + idEdge++, DEPENDENCY, e.getKey(), d._1(), d._2(), null);
                for (String v : versions.get(d._1())) {
                    graph.addEdge("e" + idEdge++, CHANGE, e.getKey(), v, null, null); // TODO: get metrics
                }
            }
        }
        // return
        return graph;
    }

    // examples : make 1 static final method for each
    public static final UpdateGraph<UpdateNode, UpdateEdge> example001() {
        String root = "a:a:1";
        List<String> artifacts = List.of("b:b", "h:h", "e:e");
        List<String> releases = List.of("b:b:1", "b:b:2", "h:h:1", "h:h:2", "e:e:1", "e:e:2");
        Map<String, List<String>> versions = Map.of(
                "b:b", List.of("b:b:1", "b:b:2"),
                "h:h", List.of("h:h:1", "h:h:2"),
                "e:e", List.of("e:e:1", "e:e:2"));
        Map<String, List<Tuple2<String, String>>> dependencies = Map.of(
                "a:a:1", List.of(Tuple.of("b:b", "2")),
                "b:b:1", List.of(Tuple.of("h:h", "1")),
                "b:b:2", List.of(Tuple.of("h:h", "1"), Tuple.of("e:e", "1")));
        Map<String, MetricContainer<AddedValueEnum>> qualities = Map.of();
        return generateGraph(root, artifacts, releases, versions, dependencies, qualities);
    }

    public static final UpdateGraph<UpdateNode, UpdateEdge> example002() {
        String root = "p:p:1";
        List<String> artifacts = List.of("x:l1", "x:l2", "x:l3", "x:l4", "x:l5", "x:l6");
        List<String> releases = List.of(
                "x:l1:1", "x:l1:2", "x:l1:3",
                "x:l2:1", "x:l2:2",
                "x:l3:1",
                "x:l4:1",
                "x:l5:1",
                "x:l6:1", "x:l6:2");
        Map<String, List<String>> versions = Map.of(
                "x:l1", List.of("x:l1:1", "x:l1:2", "x:l1:3"),
                "x:l2", List.of("x:l2:1", "x:l2:2"),
                "x:l3", List.of("x:l3:1"),
                "x:l4", List.of("x:l4:1"),
                "x:l5", List.of("x:l5:1"),
                "x:l6", List.of("x:l6:1", "x:l6:2"));
        Map<String, List<Tuple2<String, String>>> dependencies = new HashMap<>();
        dependencies.put("p:p:1", List.of(Tuple.of("x:l1", "1"), Tuple.of("x:l2", "1")));
        dependencies.put("x:l1:1", List.of(Tuple.of("x:l3", "1"), Tuple.of("x:l4", "1")));
        dependencies.put("x:l1:2", List.of(Tuple.of("x:l4", "1"), Tuple.of("x:l5", "1")));
        dependencies.put("x:l1:3", List.of(Tuple.of("x:l5", "2"), Tuple.of("x:l2", "2")));
        dependencies.put("x:l2:1", List.of());
        dependencies.put("x:l2:2", List.of(Tuple.of("x:l6", "1")));
        dependencies.put("x:l3:1", List.of());
        dependencies.put("x:l4:1", List.of());
        dependencies.put("x:l5:1", List.of(Tuple.of("x:l6", "2")));
        dependencies.put("x:l6:1", List.of());
        dependencies.put("x:l6:2", List.of());
        Map<String, MetricContainer<AddedValueEnum>> qualities = new HashMap<>();
        // FIXME: root needs info too
        qualities.put("p:p:1", new MetricMap<>(Map.of(CVE, 0.0, FRESHNESS, 0.0, POPULARITY, 0.0)));
        // same all for l1 versions 1-2-3
        qualities.put("x:l1:1", new MetricMap<>(Map.of(CVE, 0.0, FRESHNESS, 10.0, POPULARITY, 0.0)));
        qualities.put("x:l1:2", new MetricMap<>(Map.of(CVE, 0.0, FRESHNESS, 5.0, POPULARITY, 0.0)));
        qualities.put("x:l1:3", new MetricMap<>(Map.of(CVE, 0.0, FRESHNESS, 0.0, POPULARITY, 0.0)));
        // l2 version 1 is less fresh but more popular than l2 version 2
        qualities.put("x:l2:1", new MetricMap<>(Map.of(CVE, 0.0, FRESHNESS, 5.0, POPULARITY, 0.0)));
        qualities.put("x:l2:2", new MetricMap<>(Map.of(CVE, 0.0, FRESHNESS, 0.0, POPULARITY, 5.0)));
        // l6 version 1 is less fresh but more popular than l2 version 2
        qualities.put("x:l6:1", new MetricMap<>(Map.of(CVE, 0.0, FRESHNESS, 5.0, POPULARITY, 0.0)));
        qualities.put("x:l6:2", new MetricMap<>(Map.of(CVE, 0.0, FRESHNESS, 0.0, POPULARITY, 2.8)));
        // neutral info for l3 version 1 (single version, end of graph)
        qualities.put("x:l3:1", new MetricMap<>(Map.of(CVE, 0.0, FRESHNESS, 0.0, POPULARITY, 0.0)));
        // neutral info for l4 version 1 (single version, end of graph)
        qualities.put("x:l4:1", new MetricMap<>(Map.of(CVE, 0.0, FRESHNESS, 0.0, POPULARITY, 0.0)));
        // neutral info for l5 version 1 (single version, end of graph)
        qualities.put("x:l5:1", new MetricMap<>(Map.of(CVE, 0.0, FRESHNESS, 0.0, POPULARITY, 0.0)));
        return generateGraph(root, artifacts, releases, versions, dependencies, qualities);
    }

    @Override
    public Set<UpdateEdge> outgoingEdgesOf(UpdateNode node) {
        return edges.stream().filter(e -> source(e) == node).collect(Collectors.toSet());
    }

    @Override
    public void removeNode(UpdateNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeNode'");
    }

    @Override
    public UpdateGraph<UpdateNode, UpdateEdge> copy() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'copy'");
    }

    @Override
    public Optional<UpdateNode> currentDependencyRelease(UpdateNode release, UpdateNode artifact) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'currentDependencyRelease'");
    }
}
