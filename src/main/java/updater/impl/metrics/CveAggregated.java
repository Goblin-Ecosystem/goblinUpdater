package updater.impl.metrics;

import org.json.simple.JSONObject;

import updater.api.metrics.MetricType;

public class CveAggregated extends Cve {
    public CveAggregated(JSONObject nodeJsonObject) {
        super(nodeJsonObject);
    }

    @Override
    public MetricType type() {
        return MetricType.CVE_AGGREGATED;
    }
}
