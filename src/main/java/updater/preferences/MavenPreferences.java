package updater.preferences;

import addedvalue.AddedValueEnum;
import org.yaml.snakeyaml.Yaml;
import util.LoggerHelpers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class MavenPreferences implements UpdatePreferences {
    private final Map<AddedValueEnum, Double> metricsAndCoefMap;

    public MavenPreferences(Path path) {
        Map<String, Object> confMap = getYmlMap(path);
        this.metricsAndCoefMap = generateMetricAndCoefMap(confMap);
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

    // FIXME: should this logic be here or in an updater?
    @Override
    public double coefficientFor(AddedValueEnum addedValueEnum) {
        if (metricsAndCoefMap.get(addedValueEnum) == null && addedValueEnum.isAggregated()) {
            return metricsAndCoefMap.get(addedValueEnum.notAggregatedVersion()) == null ? 0.0
                    : metricsAndCoefMap.get(addedValueEnum.notAggregatedVersion());
        }
        return metricsAndCoefMap.get(addedValueEnum) == null ? 0.0 : metricsAndCoefMap.get(addedValueEnum);
    }

    private Map<AddedValueEnum, Double> generateMetricAndCoefMap(Map<String, Object> confMap) {
        // FIXME: hides field at line 14.
        Map<AddedValueEnum, Double> metricsAndCoefMap = new HashMap<>();
        for (Map<String, Object> map : (List<Map<String, Object>>) confMap.get("metrics")) {
            metricsAndCoefMap.put(AddedValueEnum.valueOf(map.get("metric").toString().toUpperCase()),
                    Double.parseDouble(map.get("coef").toString()));
        }
        return metricsAndCoefMap;
    }

    private Map<String, Object> getYmlMap(Path path) {
        Yaml yaml = new Yaml();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(path.toFile());
        } catch (FileNotFoundException e) {
            LoggerHelpers.fatal("Fail to load the conf file:\n" + e.getMessage());
        }
        return yaml.load(inputStream);
    }
}
