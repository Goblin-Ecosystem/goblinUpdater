package updater.impl.metrics;

import org.json.simple.JSONObject;

import updater.api.metrics.Metric;
import updater.api.metrics.MetricType;
import static updater.impl.metrics.SimpleMetricType.*;

public class Freshness implements Metric {
    private final JSONObject valueJsonObject;

    public Freshness(JSONObject nodeJsonObject) {
        this.valueJsonObject = (JSONObject) nodeJsonObject.get(type().toJsonKey());
    }

    @Override
    public MetricType type() {
        return FRESHNESS;
    }

    @Override
    public double compute() {
        return Double.parseDouble(valueJsonObject.get("numberMissedRelease").toString());
    }
}
