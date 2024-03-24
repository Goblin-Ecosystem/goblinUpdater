package updater.impl.metrics;

import org.json.simple.JSONObject;

import updater.api.metrics.MetricType;
import static updater.impl.metrics.SimpleMetricType.*;

public class CveAggregated extends Cve {
    public CveAggregated(JSONObject nodeJsonObject) {
        super(nodeJsonObject);
    }

    @Override
    public MetricType type() {
        return CVE_AGGREGATED;
    }
}
