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

    private static final double K = 1000;

    // normalization [max, min] -> [0, 1]
    private final double normalizeMaxTo0MinTo1(double value, double max, double min) {
        if(max-min == 0){
            return 0;
        }
        return (max - value) / (max - min);
    }

    // normalization [min, max] -> [0, 1]
    private final double normalizeMinTo0MaxTo1(double value, double max, double min) {
        return 1 - normalizeMaxTo0MinTo1(value, max, min);
    }

    private final double normalize(double value, double max, double min, MetricType metricType, double k) {
        double normalizedValue;
        if (metricType.biggerIsBetter()) {
            // for a metric where bigger is better, and since we minimize the value
            // the maximum must be 0 and the minimum must be 1
            normalizedValue = normalizeMaxTo0MinTo1(value, max, min);
        } else {
            // for a metric where smaller is better, and since we minimize the value
            // the minimum must be 0 and the maximum must be 0
            normalizedValue = normalizeMinTo0MaxTo1(value, max, min);
        }
        return k * normalizedValue;
    }

    @Override
    public void normalize(UpdateGraph<UpdateNode, UpdateEdge> graph){
        Map<MetricType, Double> mapMetricMax = new HashMap<>();
        Map<MetricType, Double> mapMetricMin = new HashMap<>();
        // looking for min-max values for release nodes
        for (UpdateNode release : graph.releaseNodes()) {
            for (MetricType metricType : release.contentTypes()) {
                release.get(metricType).ifPresent(value -> {
                        mapMetricMax.merge(metricType, value, Math::max);
                        mapMetricMin.merge(metricType, value, Math::min); });
            }
        }
        // looking for max values for change edges (cost)
        for (ChangeEdge changeEdge : graph.outgoingEdgesOf(graph.rootNode().get()).stream().filter(UpdateEdge::isChange).map(ChangeEdge.class::cast).toList()){
            for (MetricType metricType : changeEdge.contentTypes()) {
                changeEdge.get(metricType).ifPresent(value -> { 
                        mapMetricMax.merge(metricType, value, Math::max);
                        mapMetricMin.merge(metricType, value, Math::min); });
            }
        }
        // Normalize release nodes
        for (UpdateNode release : graph.releaseNodes()) {
            for (MetricType metricType : release.contentTypes()) {
                double min = mapMetricMin.get(metricType);
                double max = mapMetricMax.get(metricType);
                Optional<Double> score = release.get(metricType);
                score.ifPresent(aDouble -> release.put(metricType, normalize(score.get(), max, min, metricType, K)));
            }
        }
        // Normalize change edge cost
        for (ChangeEdge changeEdge : graph.outgoingEdgesOf(graph.rootNode().get()).stream().filter(UpdateEdge::isChange).map(ChangeEdge.class::cast).toList()){
            for (MetricType metricType : changeEdge.contentTypes()) {
                double min = mapMetricMin.get(metricType);
                double max = mapMetricMax.get(metricType);
                Optional<Double> score = changeEdge.get(metricType);
                score.ifPresent(aDouble -> changeEdge.put(metricType, normalize(score.get(), max, min, metricType, K)));
            }
        }
    }
}
