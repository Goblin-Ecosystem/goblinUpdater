package updater.impl.preferences;

import updater.api.metrics.MetricType;
import updater.api.preferences.Constraint;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * A simple implementation of update {@link Preferences}.
 */
public class SimplePreferences implements Preferences {
    private final Map<MetricType, Double> preferences;
    private final List<Constraint> constraints;

    public SimplePreferences(Path path) {
        Map<String, Object> confMap = getYmlMap(path);
        this.preferences = generatePreferences(confMap);
        this.constraints = generateConstraints(confMap);
    }

    public SimplePreferences(String conf) {
        Map<String, Object> confMap = getYmlMap(conf);
        this.preferences = generatePreferences(confMap);
        this.constraints = generateConstraints(confMap);
    }

    @Override
    public Set<MetricType> qualityMetrics() {
        return preferences.keySet().stream().filter(MetricType::isQualityMetric).collect(Collectors.toSet());
    }

    @Override
    public Set<MetricType> costMetrics() {
        return preferences.keySet().stream().filter(MetricType::isCostMetric).collect(Collectors.toSet());
    }

    // FIXME: should this logic be here or in an updater?
    @Override
    public Optional<Double> coefficientFor(MetricType metric) {
        if (metric == null)
            return Optional.empty();
        if (preferences.containsKey(metric))
            return Optional.of(preferences.get(metric));
        if (metric.isAggregated() && preferences.containsKey(metric.nonAggregatedVersion()))
            return Optional.of(preferences.get(metric.nonAggregatedVersion()));
        return Optional.empty();
    }

    private Map<MetricType, Double> generatePreferences(Map<String, Object> confMap) {
        final String METRICS = "metrics";
        final String METRIC = "metric";
        final String COEF = "coef";
        final String INVALID_METRIC = "invalid metric config: ";
        Map<MetricType, Double> metricsAndCoefMap = new HashMap<>();
        if ((confMap.containsKey(METRICS) && (confMap.get(METRICS) instanceof List ms))) {
            for (Object o : ms) {
                if (o instanceof Map m) {
                    Optional<Object> mType = Optional.ofNullable(m.get(METRIC));
                    Optional<Object> mCoef = Optional.ofNullable(m.get(COEF));
                    if (mType.isPresent() && (mType.get() instanceof String mt) && mCoef.isPresent()
                            && (mCoef.get() instanceof Double mc)) {
                        Optional<MetricType> omt = SimpleMetricDeclarator.instance().fromJsonKey(mt.toUpperCase());
                        if (omt.isPresent()) {
                            metricsAndCoefMap.put(omt.get(), mc);
                        } else {
                            LoggerHelpers.instance().warning(String.format("unknown metric type %s", mt));
                        }
                    } else {
                        LoggerHelpers.instance().warning(INVALID_METRIC + o);
                    }
                } else {
                    LoggerHelpers.instance().warning(INVALID_METRIC + o);
                }
            }
        } else {
            LoggerHelpers.instance().fatal("missing " + METRICS + " list in config");
        }
        return metricsAndCoefMap;
    }

    private List<Constraint> generateConstraints(Map<String, Object> confMap) {
        final String CONSTRAINTS = "constraints";
        final String CONSTRAINT = "constraint";
        final String VALUE = "value";
        final String INVALID_CONSTRAINT = "invalid constraint config: ";
        List<Constraint> generatedConstraints = new ArrayList<>();
        if (confMap.containsKey(CONSTRAINTS) && (confMap.get(CONSTRAINTS) instanceof List cs)) {
            for (Object o : cs) {
                if (o instanceof Map c) {
                    Optional<Object> cType = Optional.ofNullable(c.get(CONSTRAINT));
                    Optional<Object> cValue = Optional.ofNullable(c.get(VALUE));
                    if (cType.isPresent() && (cType.get() instanceof String ct) && cValue.isPresent()) {
                        Optional<Constraint> cc = SimpleConstraintCreator.instance().create(ct.toUpperCase(), cValue.get());
                        if (cc.isPresent()) {
                            generatedConstraints.add(cc.get());
                        } else {
                            LoggerHelpers.instance().warning(INVALID_CONSTRAINT + o);
                        }
                    } else {
                        LoggerHelpers.instance().warning(INVALID_CONSTRAINT + o);
                    }
                } else {
                    LoggerHelpers.instance().warning(INVALID_CONSTRAINT + o);
                }
            }
        } else {
            LoggerHelpers.instance().info("no "+CONSTRAINTS+" list in config");
        }
        return generatedConstraints;
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

    private Map<String, Object> getYmlMap(String conf) {
        Yaml yaml = new Yaml();
        return yaml.load(conf);
    }

    @Override
    public boolean hasConstraints() {
        return !this.constraints.isEmpty();
    }

    // FIXME: data leak
    @Override
    public List<Constraint> constraints() {
        return this.constraints;
    }
}
