package updater.api.metrics;

import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;

public interface MetricNormalizer {
    void normalize(UpdateGraph<UpdateNode, UpdateEdge> graph);
}
