package code.graphs.impl;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Map.Entry;

import static code.graphs.impl.GraphMock001.NodeType.*;
import static code.graphs.impl.GraphMock001.EdgeType.*;

import java.util.ArrayList;

import code.graphs.api.Graph;
import code.metrics.api.Metric;
import code.metrics.api.MetricContainer;
import code.metrics.impl.MetricMap;
import code.update.api.UpdateEdge;
import code.update.api.UpdateNode;

import java.util.Optional;

public class GraphMock001 implements Graph<GraphMock001.Node001, GraphMock001.Edge001> {

    public enum NodeType {
        LIBRARY, ARTIFACT;
    }

    public enum EdgeType {
        VERSION, DEPENDENCY, POSSIBLE;
    }

    public record Node001(String id, NodeType type, MetricContainer metrics) implements MetricContainer, UpdateNode {

        @Override
        public Set<Metric> usedMetrics() {
            return metrics.usedMetrics();
        }

        @Override
        public void addMetric(Metric m, Double value) {
            metrics.addMetric(m, value);
        }

        @Override
        public Optional<Double> getValue(Metric metric) {
            return metrics.getValue(metric);
        }

        @Override
        public boolean isArtifact() {
            return type == ARTIFACT;
        }

        @Override
        public boolean isLibrary() {
            return type == LIBRARY;
        }

        @Override
        public String name() {
            return id;
        }
    }

    public record Edge001(String id, EdgeType type, Node001 source, Node001 target, MetricContainer metrics)
            implements MetricContainer, UpdateEdge {

        @Override
        public Set<Metric> usedMetrics() {
            return metrics.usedMetrics();
        }

        @Override
        public void addMetric(Metric m, Double value) {
            metrics.addMetric(m, value);
        }

        @Override
        public Optional<Double> getValue(Metric metric) {
            return metrics.getValue(metric);
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
        public boolean isPossible() {
            return type == POSSIBLE;
        }

        @Override
        public String name() {
            return id;
        }

    }

    private List<Node001> nodes;
    private List<Edge001> edges;
    private Node001 root;

    private Node001 addNode(String id, NodeType type, Map<Metric, Double> metrics) {
        Node001 node = new Node001(id, type, new MetricMap(metrics));
        nodes.add(node);
        return node;
    }

    private Edge001 addEdge(String id, EdgeType type, String source, String target,
            Map<Metric, Double> metrics) {
        Optional<Node001> ns = getNodeById(source);
        Optional<Node001> nt = getNodeById(target);
        if (ns.isEmpty())
            throw new IllegalArgumentException("source node not found");
        if (nt.isEmpty())
            throw new IllegalArgumentException("target node not found");
        Edge001 edge = new Edge001(id, type, ns.get(), nt.get(), new MetricMap(metrics));
        edges.add(edge);
        return edge;
    }

    private void setRoot(Node001 root) {
        this.root = root;
    }

    private GraphMock001() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        root = null;
    }

    @Override
    public List<Node001> nodes() {
        return nodes;
    }

    @Override
    public List<Edge001> edges() {
        return edges;
    }

    private Optional<Node001> getNodeById(String id) {
        return nodes.stream().filter(n -> n.id().equals(id)).findFirst();
    }

    @Override
    public Node001 source(Edge001 e) {
        return e.source();
    }

    @Override
    public Node001 target(Edge001 e) {
        return e.target();
    }

    @Override
    public Optional<Node001> rootNode() {
        return Optional.ofNullable(root);
    }

    // examples : make 1 static final method for each
    public static final GraphMock001 example001() {
        // CHANGE HERE
        GraphMock001 graph = new GraphMock001();
        String root = "a";
        List<String> artifacts = List.of(
                "c", "d", "i", "j", "f", "g");
        List<String> libraries = List.of(
                "b", "h", "e");
        Map<String, List<String>> versions = Map.of(
                "b", List.of("c", "d"),
                "h", List.of("i", "j"),
                "e", List.of("f", "g"));
        Map<String, List<String>> dependencies = Map.of(
            "a", List.of("b"),
             "c", List.of("h"),
             "d", List.of("h", "e")
        );
        // DO NOT CHANGE BELOW (to be refactored to be reusable)
        // setup
        int idEdge = 0;
        // root
        Node001 rootNode = graph.addNode(root, ARTIFACT, Map.of());
        graph.setRoot(rootNode);
        // other artifacts
        for (String a : artifacts) {
            graph.addNode(a, ARTIFACT, Map.of());
        }
        // other versions
        for (String l : libraries) {
            graph.addNode(l, LIBRARY, Map.of());

        }
        // versions
        for (Entry<String, List<String>> e : versions.entrySet()) {
            for (String v : e.getValue()) {
                graph.addEdge("e" + idEdge++, VERSION, e.getKey(), v, Map.of());
            }
        }
        // dependencies and possibles
        for (Entry<String, List<String>> e : dependencies.entrySet()) {
            for (String d : e.getValue()) {
                graph.addEdge("e" + idEdge++, DEPENDENCY, e.getKey(), d, Map.of());
                for (String v : versions.get(d)) {
                    graph.addEdge("e" + idEdge++, POSSIBLE, e.getKey(), v, Map.of());
                }
            }
        }
        // return
        return graph;
    }
}
