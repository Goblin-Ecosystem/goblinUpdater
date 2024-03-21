package graph.structures.mocks;

import java.util.List;
import java.util.Set;

import addedvalue.AddedValueEnum;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

import graph.entities.nodes.UpdateNode;
import graph.entities.edges.UpdateEdge;
import graph.structures.UpdateGraph;

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

    public record Node001(String id, NodeType type) implements UpdateNode {

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

    public record Edge001(String id, EdgeType type, UpdateNode source, UpdateNode target)
            implements UpdateEdge {

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

    private UpdateNode addNode(String id, NodeType type) {
        UpdateNode node = new Node001(id, type);
        addNode(node);
        return node;
    }

    @Override
    public void addNode(UpdateNode node) {
        nodes.add(node);
    }

    private UpdateEdge addEdge(String id, EdgeType type, String source, String target) {
        Optional<UpdateNode> ns = getNodeById(source);
        Optional<UpdateNode> nt = getNodeById(target);
        if (ns.isEmpty())
            throw new IllegalArgumentException("source node not found");
        if (nt.isEmpty())
            throw new IllegalArgumentException("target node not found");
        UpdateEdge edge = new Edge001(id, type, ns.get(), nt.get());
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
            List<String> releases, Map<String, List<String>> versions, Map<String, List<String>> dependencies) {
        GraphMock001 graph = new GraphMock001();
        int idEdge = 0;
        // root
        UpdateNode rootNode = graph.addNode(root, RELEASE);
        graph.setRoot(rootNode);
        // other releases
        for (String r : releases) {
            graph.addNode(r, RELEASE);
        }
        // artifacts
        for (String a : artifacts) {
            graph.addNode(a, ARTIFACT);
        }
        // versions
        for (Entry<String, List<String>> e : versions.entrySet()) {
            for (String v : e.getValue()) {
                graph.addEdge("e" + idEdge++, VERSION, e.getKey(), v);
            }
        }
        // dependencies and possibles
        for (Entry<String, List<String>> e : dependencies.entrySet()) {
            for (String d : e.getValue()) {
                graph.addEdge("e" + idEdge++, DEPENDENCY, e.getKey(), d);
                for (String v : versions.get(d)) {
                    graph.addEdge("e" + idEdge++, CHANGE, e.getKey(), v);
                }
            }
        }
        // return
        return graph;
    }

    // examples : make 1 static final method for each
    public static final UpdateGraph<UpdateNode, UpdateEdge> example001() {
        String root = "a";
        List<String> artifacts = List.of("b", "h", "e");
        List<String> releases = List.of("c", "d", "i", "j", "f", "g");
        Map<String, List<String>> versions = Map.of(
                "b", List.of("c", "d"),
                "h", List.of("i", "j"),
                "e", List.of("f", "g"));
        Map<String, List<String>> dependencies = Map.of(
                "a", List.of("b"),
                "c", List.of("h"),
                "d", List.of("h", "e"));
        return generateGraph(root, artifacts, releases, versions, dependencies);
    }

    public static final UpdateGraph<UpdateNode, UpdateEdge> example002() {
        String root = "p";
        List<String> artifacts = List.of("l1", "l2", "l3", "l4", "l5", "l6");
        List<String> releases = List.of(
                "l1-1", "l1-2", "l1-3",
                "l2-1", "l2-2",
                "l3-1",
                "l4-1",
                "l5-1",
                "l6-1", "l6-2");
        Map<String, List<String>> versions = Map.of(
                "l1", List.of("l1-1", "l1-2", "l1-3"),
                "l2", List.of("l2-1", "l2-2"),
                "l3", List.of("l3-1"),
                "l4", List.of("l4-1"),
                "l5", List.of("l5-1"),
                "l6", List.of("l6-1", "l6-2"));
        Map<String, List<String>> dependencies = new HashMap<>();
        dependencies.put("p", List.of("l1", "l2"));
        dependencies.put("l1-1", List.of("l3", "l4"));
        dependencies.put("l1-2", List.of("l4", "l5"));
        dependencies.put("l1-3", List.of("l5", "l2"));
        dependencies.put("l2-1", List.of());
        dependencies.put("l2-2", List.of("l6"));
        dependencies.put("l3-1", List.of());
        dependencies.put("l4-1", List.of());
        dependencies.put("l5-1", List.of("l6"));
        dependencies.put("l6-1", List.of());
        dependencies.put("l6-2", List.of());
        return generateGraph(root, artifacts, releases, versions, dependencies);
    }

    @Override
    public void removeNode(UpdateNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeNode'");
    }

    @Override
    public Set<UpdateEdge> outgoingEdgesOf(UpdateNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'outgoingEdgesOf'");
    }

    @Override
    public Set<UpdateEdge> getPossibleEdgesOf(UpdateNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPossibleEdgesOf'");
    }

    @Override
    public Node001 getCurrentUseReleaseOfArtifact(UpdateNode artifact) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCurrentUseReleaseOfArtifact'");
    }

    @Override
    public Set<UpdateNode> getRootArtifactDirectDep() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRootArtifactDirectDep'");
    }

    @Override
    public Set<UpdateNode> getAllArtifactRelease(UpdateNode artifact) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllArtifactRelease'");
    }

    @Override
    public UpdateGraph<UpdateNode, UpdateEdge> copy() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'copy'");
    }
}
