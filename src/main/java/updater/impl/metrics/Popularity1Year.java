package updater.impl.metrics;

import org.json.simple.JSONObject;
import updater.api.metrics.Metric;
import updater.api.metrics.MetricType;

import static updater.impl.metrics.SimpleMetricType.POPULARITY_1_YEAR;

public class Popularity1Year implements Metric {
    private double score;

    public Popularity1Year(JSONObject nodeJsonObject) {
        score = Integer.parseInt(nodeJsonObject.get(type().toJsonKey()).toString());
        score = score != 0 ? (1 / score) * 1000 : 1000;
    }

    @Override
    public MetricType type() {
        return POPULARITY_1_YEAR;
    }

    @Override
    public double compute() {
        return score;
    }
}
