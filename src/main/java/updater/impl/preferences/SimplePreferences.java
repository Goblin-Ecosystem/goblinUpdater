package updater.impl.preferences;

import updater.api.metrics.MetricType;
import updater.api.preferences.Preferences;
import updater.impl.metrics.SimpleMetricDeclarator;
import util.helpers.system.LoggerHelpers;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * A simple implementation of update {@link Preferences}.
 */
public class SimplePreferences implements Preferences {
    private final Map<MetricType, Double> metricsAndCoefMap;

    public SimplePreferences(Path path) {
        Map<String, Object> confMap = getYmlMap(path);
        this.metricsAndCoefMap = generateMetricAndCoefMap(confMap);
    }

    @Override
    public Set<MetricType> qualityMetrics() {
        return metricsAndCoefMap.keySet().stream().filter(MetricType::isQualityMetric).collect(Collectors.toSet());
    }

    @Override
    public Set<MetricType> costMetrics() {
        return metricsAndCoefMap.keySet().stream().filter(MetricType::isCostMetric).collect(Collectors.toSet());
    }

    // FIXME: should this logic be here or in an updater?
    @Override
    public double coefficientFor(MetricType metric) {
        if (metric == null)
            return 0.0;
        if (metricsAndCoefMap.containsKey(metric))
            return metricsAndCoefMap.get(metric);
        if (metric.isAggregated() && metricsAndCoefMap.containsKey(metric.nonAggregatedVersion()))
            return metricsAndCoefMap.get(metric.nonAggregatedVersion());
        return 0.0;
    }

    private Map<MetricType, Double> generateMetricAndCoefMap(Map<String, Object> confMap) {
        // FIXME: hides field at line 24.
        Map<MetricType, Double> metricsAndCoefMap = new HashMap<>();
        for (Map<String, Object> map : (List<Map<String, Object>>) confMap.get("metrics")) {
            Optional<MetricType> metric = SimpleMetricDeclarator.instance().fromJsonKey(map.get("metric").toString().toUpperCase());
            if (metric.isPresent()) {
                metricsAndCoefMap.put(metric.get(), Double.parseDouble(map.get("coef").toString()));
            }
        }
        return metricsAndCoefMap;
    }

    private Map<String, Object> getYmlMap(Path path) {
        Yaml yaml = new Yaml();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(path.toFile());
        } catch (FileNotFoundException e) {
            LoggerHelpers.instance().fatal("Fail to load the conf file:\n" + e.getMessage());
        }
        return yaml.load(inputStream);
    }
}
