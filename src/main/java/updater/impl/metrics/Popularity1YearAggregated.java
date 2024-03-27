package updater.impl.metrics;

import org.json.simple.JSONObject;
import updater.api.metrics.MetricType;

import static updater.impl.metrics.SimpleMetricType.POPULARITY_1_YEAR_AGGREGATED;

public class Popularity1YearAggregated extends Popularity1Year{
    public Popularity1YearAggregated(JSONObject nodeJsonObject) {
        super(nodeJsonObject);
    }

    @Override
    public MetricType type() {
        return POPULARITY_1_YEAR_AGGREGATED;
    }
}
