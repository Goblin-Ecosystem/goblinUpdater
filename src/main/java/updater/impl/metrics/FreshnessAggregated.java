package updater.impl.metrics;

import org.json.simple.JSONObject;

import updater.api.metrics.MetricType;
import static updater.impl.metrics.SimpleMetricType.*;

public class FreshnessAggregated extends Freshness {
    public FreshnessAggregated(JSONObject nodeJsonObject) {
        super(nodeJsonObject);
    }

    @Override
    public MetricType type() {
        return FRESHNESS_AGGREGATED;
    }
}
