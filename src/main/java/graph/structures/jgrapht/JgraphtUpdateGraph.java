package graph.structures.jgrapht;

import graph.entities.edges.DependencyEdge;
import graph.entities.edges.ChangeEdge;
import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.ReleaseNode;
import graph.entities.nodes.UpdateNode;
import updater.api.graph.UpdateGraph;
import util.helpers.LoggerHelpers;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class JgraphtUpdateGraph extends JgraphtCustomGraph implements UpdateGraph<UpdateNode, UpdateEdge> {

    public JgraphtUpdateGraph() {
        super();
    }

    public JgraphtUpdateGraph(Graph<UpdateNode, UpdateEdge> graphCopy) {
        super(graphCopy);
    }

    @Override
    public Optional<UpdateNode> currentDependencyRelease(UpdateNode release, UpdateNode artifact) {
        UpdateEdge edge = outgoingEdgesOf(release).stream()
               .filter(e -> e.isDependency())
               .filter(e -> target(e).equals(artifact))
               .findFirst().orElse(null);
        if (edge == null) {
            LoggerHelpers.error("Fail to get current release of: " + artifact.id());
            return null;
        } else {
            String versionId = artifact.id() + ":" + edge.targetVersion();
            return getNode(versionId);
        }
    }

    @Override
    public UpdateGraph<UpdateNode, UpdateEdge> copy() {
        Graph<UpdateNode, UpdateEdge> graphCopy = new DefaultDirectedGraph<>(UpdateEdge.class);
        Graphs.addGraph(graphCopy, graph);
            return new JgraphtUpdateGraph(graphCopy);
    }

}
