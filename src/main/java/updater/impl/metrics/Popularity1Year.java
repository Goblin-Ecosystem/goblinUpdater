package updater.impl.metrics;

import org.json.simple.JSONObject;
import updater.api.metrics.Metric;
import updater.api.metrics.MetricType;

import static updater.impl.metrics.SimpleMetricType.POPULARITY_1_YEAR;

public class Popularity1Year implements Metric {
    private final int value;

    public Popularity1Year(JSONObject nodeJsonObject) {
        this.value = Integer.parseInt(nodeJsonObject.get(type().toJsonKey()).toString());
    }

    @Override
    public MetricType type() {
        return POPULARITY_1_YEAR;
    }

    @Override
    public double compute() {
        return value;
    }
}
