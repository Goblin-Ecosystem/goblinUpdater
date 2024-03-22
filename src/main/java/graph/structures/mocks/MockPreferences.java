package graph.structures.mocks;

import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

import addedvalue.AddedValueEnum;
import updater.preferences.UpdatePreferences;

// TODO: not DRY wrt MavenPreferences
public class MockPreferences implements UpdatePreferences {

    // FIXME: lots of duplication with MavenPreferences
    private Map<AddedValueEnum, Double> metricsAndCoefMap;

    public MockPreferences(Map<AddedValueEnum, Double> metricsAndCoefMap) {
        this.metricsAndCoefMap = metricsAndCoefMap;
    }

    @Override
    public Set<AddedValueEnum> qualityMetrics() {
        return metricsAndCoefMap.keySet().stream().filter(AddedValueEnum::isQualityMetric).collect(Collectors.toSet());
    }

    @Override
    public Set<AddedValueEnum> costMetrics() {
        return metricsAndCoefMap.keySet().stream().filter(AddedValueEnum::isCostMetric).collect(Collectors.toSet());
    }

    @Override
    public Set<AddedValueEnum> qualityMetricsAggregated() {
        return metricsAndCoefMap.keySet().stream()
                .map(m -> m.isAggregated() ? m : m.aggregatedVersion())
                .collect(Collectors.toSet());
    }

    @Override
    public double coefficientFor(AddedValueEnum addedValueEnum) {
        return (addedValueEnum == null || metricsAndCoefMap.get(addedValueEnum) == null) ? 0.0
                : metricsAndCoefMap.get(addedValueEnum);
    }

}
