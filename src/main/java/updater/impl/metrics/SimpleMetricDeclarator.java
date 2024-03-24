package updater.impl.metrics;

import updater.api.metrics.Metric;
import updater.api.metrics.MetricDeclarator;
import updater.api.metrics.MetricType;
import static updater.api.metrics.MetricType.*;

import java.util.Optional;
import java.util.Map;

public class SimpleMetricDeclarator implements MetricDeclarator {

    /**
     * This map could be populated using a configuration file.
     */
    static final Map<MetricType, Class<? extends Metric>> metrics;
    static {
        metrics = Map.of(
                CVE, Cve.class,
                CVE_AGGREGATED, CveAggregated.class,
                FRESHNESS, Freshness.class,
                FRESHNESS_AGGREGATED, FreshnessAggregated.class);
    }

    private SimpleMetricDeclarator() {
    }

    private static final MetricDeclarator instance = new SimpleMetricDeclarator();

    public static MetricDeclarator instance() {
        return instance;
    }

    @Override
    public Optional<Class<? extends Metric>> metric(MetricType type) {
        return Optional.ofNullable(metrics.get(type));
    }

}
