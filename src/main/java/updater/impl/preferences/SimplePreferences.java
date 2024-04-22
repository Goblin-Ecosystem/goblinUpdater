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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

// TODO: not DRY
/**
 * A simple implementation of update {@link Preferences}.
 */
public class SimplePreferences implements Preferences {
    private Map<MetricType, Double> preferences;
    private List<Constraint> constraints;
    private Focus releaseFocus;
    private Focus changeFocus;
    private Set<Selector> releaseSelectors;
    private double defaultCost;

    public static final double HIGH_COST = 999999.9;
    public static final double LOW_COST = 0.0;

    public SimplePreferences(Path path) {
        this.setup(getYmlMap(path));
    }

    public SimplePreferences(String conf) {
        this.setup(getYmlMap(conf));
    }

    private void setup(Map<String, Object> conf) {
        this.preferences = generatePreferences(conf);
        this.constraints = generateConstraints(conf);
        this.releaseFocus = generateReleaseFocus(conf);
        this.releaseSelectors = generateReleaseSelectors(conf);
        this.changeFocus = generateChangeFocus(conf);
        this.defaultCost = generateDefaultCost(conf);
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
    
    private Focus generateReleaseFocus(Map<String, Object> confMap) {
        final String RELEASES = "releases";
        final String FOCUS = "focus";
        if (confMap.containsKey(RELEASES) && confMap.get(RELEASES) instanceof Map rs && (rs.containsKey(FOCUS) && rs.get(FOCUS) instanceof String f)) {
            try {
                return Focus.valueOf(f.toUpperCase());
            } catch (Exception e) {
                LoggerHelpers.instance().warning("unknown focus strategy for releases: " + f);
            }
        }
        LoggerHelpers.instance().warning("focus strategy for releases is undefined or ill-defined, focus ALL is used)");
        return Focus.ALL;
    }

    private Focus generateChangeFocus(Map<String, Object> confMap) {
        final String COSTS = "costs";
        final String FOCUS = "focus";
        if (confMap.containsKey(COSTS) && confMap.get(COSTS) instanceof Map cs && (cs.containsKey(FOCUS) && cs.get(FOCUS) instanceof String f)) {
            try {
                return Focus.valueOf(f.toUpperCase());
            } catch (Exception e) {
                LoggerHelpers.instance().warning("unknown focus strategy for costs: " + f);
            }
        }
        LoggerHelpers.instance().warning("focus strategy for costs is undefined or ill-defined, focus ALL is used)");
        return Focus.ALL;
    }

    private Set<Selector> generateReleaseSelectors(Map<String, Object> confMap) {
        final String RELEASES = "releases";
        final String SELECTORS = "selectors";
        Set<Selector> selectors = new HashSet<>();
        if (confMap.containsKey(RELEASES) && confMap.get(RELEASES) instanceof Map rs && (rs.containsKey(SELECTORS) && rs.get(SELECTORS) instanceof List ss)) {
            for (Object o: ss) {
                if (o instanceof String s) {
                    try {
                        Selector selector = Selector.valueOf(s);
                        selectors.add(selector);
                    } catch (Exception e) {
                        LoggerHelpers.instance().warning("unknown selector " + s);
                    }
                } else {
                    LoggerHelpers.instance().warning("unknown selector " + o);
                }
            }
        } else {
            LoggerHelpers.instance().warning("selector strategy for releases is undefined or ill-defined, selectors [] is used)");
        }
        return selectors;
    }

    private double generateDefaultCost(Map<String, Object> confMap) {
        final String COSTS = "costs";
        final String DEFAULT_COST = "default";
        if (confMap.containsKey(COSTS) && confMap.get(COSTS) instanceof Map cs && (cs.containsKey(DEFAULT_COST) && cs.get(DEFAULT_COST) instanceof Double dc)) {
            return dc;
        }
        LoggerHelpers.instance().warning("default value for costs is undefined or ill-defined, value "+HIGH_COST+" is used)");
        return HIGH_COST;
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

    @Override
    public Focus releaseFocus() {
        return this.releaseFocus;
    }

    // FIXME: data leak
    @Override
    public Set<Selector> releaseSelectors() {
        return this.releaseSelectors;
    }
    
    @Override
    public Focus changeFocus() {
        return this.changeFocus;
    }

    @Override 
    public double defaultCost() {
        return this.defaultCost;
    }

}