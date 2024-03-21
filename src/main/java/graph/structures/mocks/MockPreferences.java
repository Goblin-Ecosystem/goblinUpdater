package graph.structures.mocks;

import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

import addedvalue.AddedValueEnum;
import updater.preferences.UpdatePreferences;

public class MockPreferences implements UpdatePreferences {

    // FIXME: lots of duplication with MavenPreferences
    private Map<AddedValueEnum, Double> metricsAndCoefMap;

    public MockPreferences(Map<AddedValueEnum, Double> metricsAndCoefMap) {
        this.metricsAndCoefMap = metricsAndCoefMap;
    }

    @Override
    public Set<AddedValueEnum> getAddedValueEnumSet() {
        return metricsAndCoefMap.keySet();
    }

    @Override
    public Set<AddedValueEnum> getAggregatedAddedValueEnumSet() {
        return metricsAndCoefMap.keySet().stream()
                .map(m -> m.isAggregated() ? m : m.aggregatedVersion())
                .collect(Collectors.toSet());
    }

    @Override
    public double getAddedValueCoef(AddedValueEnum addedValueEnum) {
        return (addedValueEnum == null || metricsAndCoefMap.get(addedValueEnum) == null) ? 0.0
                : metricsAndCoefMap.get(addedValueEnum);
    }

}
