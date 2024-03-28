package updater.impl.mock.graph;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;
import updater.api.metrics.MetricContainer;
import updater.api.metrics.MetricType;
import updater.impl.metrics.MetricMap;
import util.helpers.system.LoggerHelpers;

import static updater.impl.metrics.SimpleMetricType.*;
import static updater.impl.mock.graph.GraphMock.EdgeType.*;
import static updater.impl.mock.graph.GraphMock.NodeType.*;

import java.util.Optional;

public class GraphMock implements UpdateGraph<UpdateNode, UpdateEdge> {

    public enum NodeType {
        RELEASE, ARTIFACT;
    }

    public enum EdgeType {
        VERSION, DEPENDENCY, CHANGE;
    }

    public record Node001(String id, NodeType type, MetricContainer<MetricType> metrics) implements UpdateNode {

        public Node001(String id, NodeType type, MetricContainer<MetricType> metrics) {
            this.id = id;
            this.type = type;
            this.metrics = metrics;
            if (!hasValidId(id))
                throw new IllegalArgumentException("Invalid node id " + id);
        }

        @Override
        public Set<MetricType> contentTypes() {
            return metrics.contentTypes();
        }

        @Override
        public void put(MetricType m, Double value) {
            metrics.put(m, value);
        }

        @Override
        public Optional<Double> get(MetricType m) {
            return metrics.get(m);
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
        public Set<MetricType> knownValues() {
            return switch (type) {
                case RELEASE -> Set.of(CVE, FRESHNESS);
                case ARTIFACT -> new HashSet<>();
                default -> Set.of();
            };
        }

    }

    public record Edge001(String id, EdgeType type, UpdateNode source, UpdateNode target, String targetVersion,
            MetricContainer<MetricType> metrics)
            implements UpdateEdge {

        @Override
        public Set<MetricType> contentTypes() {
            return metrics.contentTypes();
        }

        @Override
        public void put(MetricType m, Double value) {
            metrics.put(m, value);
        }

        @Override
        public Optional<Double> get(MetricType m) {
            return metrics.get(m);
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

    private UpdateNode addNode(String id, NodeType type, MetricContainer<MetricType> metrics) {
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
            MetricContainer<MetricType> metrics) {
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

    private GraphMock() {
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
            Map<String, MetricContainer<MetricType>> metrics) {
        GraphMock graph = new GraphMock();
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

    private static final UpdateGraph<UpdateNode, UpdateEdge> generateGraph2(String root, List<String> artifacts,
            List<String> releases, Map<String, List<String>> versions,
            Map<String, List<Tuple2<String, String>>> dependencies,
            Map<String, MetricContainer<MetricType>> metrics) {
        GraphMock graph = new GraphMock();
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
                LoggerHelpers.instance().low(e.getKey() + " -> " + d._1());
            }
        }
        // changes
        for (Entry<String, List<Tuple2<String, String>>> e : dependencies.entrySet()) {
            for (Tuple2<String, String> d : e.getValue()) {
                if (e.getKey().equals(root)) {
                    for (String v : versions.get(d._1())) {
                        graph.addEdge("e" + idEdge++, CHANGE, e.getKey(), v, null, null); // TODO: get metrics
                        LoggerHelpers.instance().low(e.getKey() + " => " + v);
                    }
                }
            }
        }
        // return
        return graph;
    }

    /**
     * generates an example with only n artifacts, and m releases for each.
     */
    public static final UpdateGraph<UpdateNode, UpdateEdge> generateExample001(int n, int m) {
        LoggerHelpers.instance().info("example generator n 1");
        List<String> artifacts = new ArrayList<>();
        List<String> releases = new ArrayList<>();
        Map<String, List<String>> versions = new HashMap<>();
        Map<String, List<Tuple2<String, String>>> dependencies = new HashMap<>();
        Map<String, MetricContainer<MetricType>> qualities = new HashMap<>();
        //
        String root = rId(0, 1);
        qualities.put(root, genMetrics());
        for (int i = 1; i <= n; i++) {
            String artifact = aId(i);
            artifacts.add(artifact);
            versions.put(artifact, new ArrayList<>());
            for (int j = 1; j <= m; j++) {
                String release = rId(i, j);
                releases.add(release);
                versions.computeIfAbsent(artifact, a -> new ArrayList<>()).add(release);
                qualities.put(release, genMetrics());
            }
        }
        return generateGraph(root, artifacts, releases, versions, dependencies, qualities);
    }

    /**
     * generates an example with only n artifacts, and m releases for each,
     * NEW wrt generateExample001: dependencies g:0:1 -1-> g:1 and g:i:x -x-> g:i+1 (1 <= i < n)
     */
    public static final UpdateGraph<UpdateNode, UpdateEdge> generateExample002(int n, int m) {
        LoggerHelpers.instance().info("example generator n 2");
        List<String> artifacts = new ArrayList<>();
        List<String> releases = new ArrayList<>();
        Map<String, List<String>> versions = new HashMap<>();
        Map<String, List<Tuple2<String, String>>> dependencies = new HashMap<>();
        Map<String, MetricContainer<MetricType>> qualities = new HashMap<>();
        // root
        String root = rId(0, 1); // g:0:1
        qualities.put(root, genMetrics());
        dependencies.computeIfAbsent(rId(0, 1), a -> new ArrayList<>()).add(Tuple.of(aId(1), "1")); // g:0:1 -1-> g:1
        // others
        for (int i = 1; i <= n; i++) {
            String artifact = aId(i); // artifact g:i
            artifacts.add(artifact);
            versions.put(artifact, new ArrayList<>());
            for (int j = 1; j <= m; j++) {
                String release = rId(i, j); // release g:i:j
                releases.add(release);
                versions.computeIfAbsent(artifact, a -> new ArrayList<>()).add(release);
                qualities.put(release, genMetrics());
                if (i < n) {
                    dependencies.computeIfAbsent(release, a -> new ArrayList<>())
                            .add(Tuple.of(aId(i + 1), String.format("%d", j))); // g:i:j -j-> g:i+1
                }
            }
        }
        return generateGraph(root, artifacts, releases, versions, dependencies, qualities);
    }

    /**
     * generates an example with only a n artifacts, and m releases for each, plus
     * dependencies g:0:1 -1-> g:1 and g:i:x -x-> g:i+1 (1 <= i < n)
     * NEW wrt generateExample002: change edges only for root
     */
    public static final UpdateGraph<UpdateNode, UpdateEdge> generateExample003(int n, int m) {
        LoggerHelpers.instance().info("example generator n 3");
        List<String> artifacts = new ArrayList<>();
        List<String> releases = new ArrayList<>();
        Map<String, List<String>> versions = new HashMap<>();
        Map<String, List<Tuple2<String, String>>> dependencies = new HashMap<>();
        Map<String, MetricContainer<MetricType>> qualities = new HashMap<>();
        // root
        String root = rId(0, 1); // g:0:1
        qualities.put(root, genMetrics());
        dependencies.computeIfAbsent(rId(0, 1), a -> new ArrayList<>()).add(Tuple.of(aId(1), "1")); // g:0:1 -1-> g:1
        // others
        for (int i = 1; i <= n; i++) {
            String artifact = aId(i); // artifact g:i
            artifacts.add(artifact);
            versions.put(artifact, new ArrayList<>());
            for (int j = 1; j <= m; j++) {
                String release = rId(i, j); // release g:i:j
                releases.add(release);
                versions.computeIfAbsent(artifact, a -> new ArrayList<>()).add(release);
                qualities.put(release, genMetrics());
                if (i < n) {
                    dependencies.computeIfAbsent(release, a -> new ArrayList<>())
                            .add(Tuple.of(aId(i + 1), String.format("%d", j))); // g:i:j -j-> g:i+1
                }
            }
        }
        return generateGraph2(root, artifacts, releases, versions, dependencies, qualities);
    }

    /**
     * generates an example with only a n artifacts, and m releases for each, plus
     * dependencies g:0:1 -1-> g:1 and g:i:x -x-> g:i+1 (1 <= i < n)
     * change edges only for root.
     * NEW wrt generateExample004: quality metrics are not 0
     */
    public static final UpdateGraph<UpdateNode, UpdateEdge> generateExample004(int n, int m) {
        LoggerHelpers.instance().info("example generator n 4");
        List<String> artifacts = new ArrayList<>();
        List<String> releases = new ArrayList<>();
        Map<String, List<String>> versions = new HashMap<>();
        Map<String, List<Tuple2<String, String>>> dependencies = new HashMap<>();
        Map<String, MetricContainer<MetricType>> qualities = new HashMap<>();
        // root
        String root = rId(0, 1); // g:0:1
        qualities.put(root, genMetrics2(null, root, 0, 1, 1));
        dependencies.computeIfAbsent(rId(0, 1), a -> new ArrayList<>()).add(Tuple.of(aId(1), "1")); // g:0:1 -1-> g:1
        // others
        for (int i = 1; i <= n; i++) {
            String artifact = aId(i); // artifact g:i
            artifacts.add(artifact);
            versions.put(artifact, new ArrayList<>());
            for (int j = 1; j <= m; j++) {
                String release = rId(i, j); // release g:i:j
                releases.add(release);
                versions.computeIfAbsent(artifact, a -> new ArrayList<>()).add(release);
                qualities.put(release, genMetrics2(artifact, release, i, j, m));
                if (i < n) {
                    dependencies.computeIfAbsent(release, a -> new ArrayList<>())
                            .add(Tuple.of(aId(i + 1), String.format("%d", j))); // g:i:j -j-> g:i+1
                }
            }
        }
        return generateGraph2(root, artifacts, releases, versions, dependencies, qualities);
    }

    private static final String aId(int id) {
        return String.format("g:%d", id);
    }

    private static final String rId(int artifactId, int id) {
        return String.format("g:%d:%d", artifactId, id);
    }

    private static final MetricContainer<MetricType> genMetrics() {
        Map<MetricType, Double> map = Map.of(
                CVE, 0.0,
                FRESHNESS, 0.0,
                POPULARITY_1_YEAR, 0.0);
        return new MetricMap<>(map);
    }

    private static final MetricContainer<MetricType> genMetrics2(String artifact, String release, int artifactId, int version, int nbVersions) {
        // NOTE: we use min-max normalization, there is also mean normalization
        Map<MetricType, Double> map = Map.of(
                CVE, 0.0,
                // freshness (HERE) = nb of more recent versions
                // for a with |versions(a)| = m . freshness(a:i) = m - i
                // (because all artifacts have m versions, no need to normalize)
                // FIXME: wrong, not always n=10 while 10 is used for freshness.
                FRESHNESS, (double)(nbVersions-version),
                // popularity (HERE) = from 2^1 to 2^10 (whatever this means the exp law is the only important thing, we could use another law)
                // so we have [1..1024]
                // then we have to normalize (and multiply by max of target interval)
                // (2^i - 1) / (2^10 - 1) * 10
                // so we have [0, 0.0009, 0.029, 0.068, ... 10]
                // and then to reverse (because 10 is better than 0)
                // 10 - ((2^i - 1) / (2^10 - 1) * 10)
                // se we have [0..10]
                POPULARITY_1_YEAR, 10 - ((Math.pow(2,version) - 1) / (1024 - 1) * 10));
        return new MetricMap<>(map);
    }

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
        Map<String, MetricContainer<MetricType>> qualities = Map.of();
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
        Map<String, MetricContainer<MetricType>> qualities = new HashMap<>();
        // FIXME: root needs info too
        qualities.put("p:p:1", new MetricMap<>(Map.of(CVE, 0.0, FRESHNESS, 0.0, POPULARITY_1_YEAR, 0.0)));
        // same all for l1 versions 1-2-3
        qualities.put("x:l1:1", new MetricMap<>(Map.of(CVE, 0.0, FRESHNESS, 10.0, POPULARITY_1_YEAR, 0.0)));
        qualities.put("x:l1:2", new MetricMap<>(Map.of(CVE, 0.0, FRESHNESS, 5.0, POPULARITY_1_YEAR, 0.0)));
        qualities.put("x:l1:3", new MetricMap<>(Map.of(CVE, 0.0, FRESHNESS, 0.0, POPULARITY_1_YEAR, 0.0)));
        // l2 version 1 is less fresh but more popular than l2 version 2
        qualities.put("x:l2:1", new MetricMap<>(Map.of(CVE, 0.0, FRESHNESS, 5.0, POPULARITY_1_YEAR, 0.0)));
        qualities.put("x:l2:2", new MetricMap<>(Map.of(CVE, 0.0, FRESHNESS, 0.0, POPULARITY_1_YEAR, 5.0)));
        // l6 version 1 is less fresh but more popular than l2 version 2
        qualities.put("x:l6:1", new MetricMap<>(Map.of(CVE, 0.0, FRESHNESS, 5.0, POPULARITY_1_YEAR, 0.0)));
        qualities.put("x:l6:2", new MetricMap<>(Map.of(CVE, 0.0, FRESHNESS, 0.0, POPULARITY_1_YEAR, 2.8)));
        // neutral info for l3 version 1 (single version, end of graph)
        qualities.put("x:l3:1", new MetricMap<>(Map.of(CVE, 0.0, FRESHNESS, 0.0, POPULARITY_1_YEAR, 0.0)));
        // neutral info for l4 version 1 (single version, end of graph)
        qualities.put("x:l4:1", new MetricMap<>(Map.of(CVE, 0.0, FRESHNESS, 0.0, POPULARITY_1_YEAR, 0.0)));
        // neutral info for l5 version 1 (single version, end of graph)
        qualities.put("x:l5:1", new MetricMap<>(Map.of(CVE, 0.0, FRESHNESS, 0.0, POPULARITY_1_YEAR, 0.0)));
        return generateGraph(root, artifacts, releases, versions, dependencies, qualities);
    }

    @Override
    public Set<UpdateEdge> outgoingEdgesOf(UpdateNode node) {
        return edges.stream().filter(e -> source(e) == node).collect(Collectors.toSet());
    }

    @Override
    public Set<UpdateEdge> incomingEdgesOf(UpdateNode node) {
        return edges.stream().filter(e -> target(e) == node).collect(Collectors.toSet());
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
