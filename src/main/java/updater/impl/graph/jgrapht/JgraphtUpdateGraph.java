package updater.impl.graph.jgrapht;

import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;
import util.helpers.system.LoggerHelpers;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.Optional;

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
            LoggerHelpers.instance().error("Fail to get current release of: " + artifact.id());
            return Optional.empty();
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
