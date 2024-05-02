package updater.impl.metrics;

import org.json.simple.JSONObject;

import updater.api.metrics.Metric;
import updater.api.metrics.MetricType;
import static updater.impl.metrics.SimpleMetricType.*;

public class Freshness implements Metric {
    private final double score;

    public Freshness(JSONObject nodeJsonObject) {
        JSONObject valueJsonObject = (JSONObject) nodeJsonObject.get(type().toJsonKey());
        score = Double.parseDouble(valueJsonObject.get("numberMissedRelease").toString());
    }

    @Override
    public MetricType type() {
        return FRESHNESS;
    }

    @Override
    public double compute() {
        return score;
    }
}
