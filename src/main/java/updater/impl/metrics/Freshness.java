package updater.impl.metrics;

import org.json.simple.JSONObject;

import updater.api.metrics.Metric;
import updater.api.metrics.MetricType;

public class Freshness implements Metric {
    private final JSONObject valueJsonObject;

    public Freshness(JSONObject nodeJsonObject) {
        this.valueJsonObject = (JSONObject) nodeJsonObject.get(type().getJsonKey());
    }

    @Override
    public MetricType type() {
        return MetricType.FRESHNESS;
    }

    @Override
    public double compute() {
        return Double.parseDouble(valueJsonObject.get("numberMissedRelease").toString());
    }
}
