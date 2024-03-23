package updater.impl.metrics;

import org.json.simple.JSONObject;

import updater.api.metrics.MetricType;

public class FreshnessAggregated extends Freshness {
    public FreshnessAggregated(JSONObject nodeJsonObject) {
        super(nodeJsonObject);
    }

    @Override
    public MetricType type() {
        return MetricType.FRESHNESS_AGGREGATED;
    }
}
