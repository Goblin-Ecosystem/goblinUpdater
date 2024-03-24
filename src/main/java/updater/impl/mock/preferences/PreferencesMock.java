package updater.impl.mock.preferences;

import java.util.Set;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import updater.api.metrics.MetricType;
import updater.api.preferences.Preferences;

// TODO: not DRY wrt MavenPreferences
public class PreferencesMock implements Preferences {

    // FIXME: lots of duplication with MavenPreferences
    private Map<MetricType, Double> metricsAndCoefMap;

    public PreferencesMock(Map<MetricType, Double> metricsAndCoefMap) {
        this.metricsAndCoefMap = metricsAndCoefMap;
    }

    @Override
    public Set<MetricType> qualityMetrics() {
        return metricsAndCoefMap.keySet().stream().filter(MetricType::isQualityMetric).collect(Collectors.toSet());
    }

    @Override
    public Set<MetricType> costMetrics() {
        return metricsAndCoefMap.keySet().stream().filter(MetricType::isCostMetric).collect(Collectors.toSet());
    }

    @Override
    public double coefficientFor(MetricType metric) {
        if (metric == null)
            return 0.0;
        return metricsAndCoefMap.get(metric) == null ? 0.0 : metricsAndCoefMap.get(metric);
    }

}
