package graph.structures.joycegraph;

import java.util.List;
import java.util.Set;

import addedvalue.AddedValueEnum;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import graph.entities.nodes.UpdateNode;
import graph.entities.edges.UpdateEdge;
import graph.structures.UpdateGraph;

import static graph.structures.joycegraph.GraphMock001.NodeType.*;
import static addedvalue.AddedValueEnum.CVE;
import static addedvalue.AddedValueEnum.FRESHNESS;
import static graph.structures.joycegraph.GraphMock001.EdgeType.*;

import java.util.Optional;

public class GraphMock001 implements UpdateGraph<GraphMock001.Node001, GraphMock001.Edge001> {

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

    public record Edge001(String id, EdgeType type, Node001 source, Node001 target)
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

    private Set<Node001> nodes;
    private Set<Edge001> edges;
    private Node001 root;

    private Node001 addNode(String id, NodeType type) {
        Node001 node = new Node001(id, type);
        nodes.add(node);
        return node;
    }

    @Override
    public void addNode(Node001 node) {
        addNode(node.id(), node.type());
    }

    private Edge001 addEdge(String id, EdgeType type, String source, String target) {
        Optional<Node001> ns = getNodeById(source);
        Optional<Node001> nt = getNodeById(target);
        if (ns.isEmpty())
            throw new IllegalArgumentException("source node not found");
        if (nt.isEmpty())
            throw new IllegalArgumentException("target node not found");
        Edge001 edge = new Edge001(id, type, ns.get(), nt.get());
        edges.add(edge);
        return edge;
    }

    @Override
    public void addEdgeFromNodeId(String fromId, String toId, Edge001 edge) {
        addEdge(edge.id(), edge.type(), fromId, toId);
    }

    private void setRoot(Node001 root) {
        this.root = root;
    }

    private GraphMock001() {
        nodes = new HashSet<>();
        edges = new HashSet<>();
        root = null;
    }

    @Override
    public Set<Node001> nodes() {
        return nodes;
    }

    @Override
    public Set<Edge001> edges() {
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
                "b", "h", "e");
        List<String> releases = List.of(
                "c", "d", "i", "j", "f", "g");
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
        Node001 rootNode = graph.addNode(root, ARTIFACT);
        graph.setRoot(rootNode);
        // other releases
        for (String r : releases) {
            graph.addNode(r, RELEASE);
        }
        // other artifacts
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

    @Override
    public void removeNode(Node001 node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeNode'");
    }

    @Override
    public Set<Edge001> outgoingEdgesOf(Node001 node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'outgoingEdgesOf'");
    }

    @Override
    public Set<Edge001> getPossibleEdgesOf(Node001 node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPossibleEdgesOf'");
    }

    @Override
    public Node001 getCurrentUseReleaseOfArtifact(Node001 artifact) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCurrentUseReleaseOfArtifact'");
    }

    @Override
    public Set<Node001> getRootArtifactDirectDep() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRootArtifactDirectDep'");
    }

    @Override
    public Set<Node001> getAllArtifactRelease(Node001 artifact) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllArtifactRelease'");
    }

    @Override
    public UpdateGraph<Node001, Edge001> copy() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'copy'");
    }
}
