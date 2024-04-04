package updater.impl.metrics;

import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;
import updater.api.metrics.MetricNormalizer;
import updater.api.metrics.MetricType;
import updater.impl.graph.structure.edges.ChangeEdge;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MetricMaxValueNormalizer implements MetricNormalizer {

    @Override
    public void normalize(UpdateGraph<UpdateNode, UpdateEdge> graph){
        Map<MetricType, Double> mapMetricMax = new HashMap<>();
        // looking for max values for release nodes
        for (UpdateNode release : graph.releaseNodes()) {
            for (MetricType metricType : release.contentTypes()) {
                release.get(metricType).ifPresent(value ->
                        mapMetricMax.merge(metricType, value, Math::max));
            }
        }
        // looking for max values for change edges (cost)
        for (ChangeEdge changeEdge : graph.outgoingEdgesOf(graph.rootNode().get()).stream().filter(UpdateEdge::isChange).map(ChangeEdge.class::cast).toList()){
            for (MetricType metricType : changeEdge.contentTypes()) {
                changeEdge.get(metricType).ifPresent(value ->
                        mapMetricMax.merge(metricType, value, Math::max));
            }
        }
        // Normalize release nodes
        for (UpdateNode release : graph.releaseNodes()) {
            for (MetricType metricType : release.contentTypes()) {
                double max = mapMetricMax.get(metricType);
                Optional<Double> score = release.get(metricType);
                score.ifPresent(aDouble -> release.put(metricType, max == 0 ? 0 : (aDouble / max)));
            }
        }
        // Normalize change edge cost
        for (ChangeEdge changeEdge : graph.outgoingEdgesOf(graph.rootNode().get()).stream().filter(UpdateEdge::isChange).map(ChangeEdge.class::cast).toList()){
            for (MetricType metricType : changeEdge.contentTypes()) {
                double max = mapMetricMax.get(metricType);
                Optional<Double> score = changeEdge.get(metricType);
                score.ifPresent(aDouble -> changeEdge.put(metricType, max == 0 ? 0 : (aDouble / max)));
            }
        }
    }
}
