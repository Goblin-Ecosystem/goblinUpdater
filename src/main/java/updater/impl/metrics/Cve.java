package updater.impl.metrics;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import updater.api.metrics.Metric;
import updater.api.metrics.MetricType;
import static updater.impl.metrics.SimpleMetricType.*;

public class Cve implements Metric {
    private double score;

    public Cve(JSONObject nodeJsonObject) {
        JSONArray valueJsonArray = (JSONArray) nodeJsonObject.get(type().toJsonKey());
        score = 0.0;
        for (Object cve : valueJsonArray) {
            JSONObject cveJson = (JSONObject) cve;
            score += getSeverityCoef(cveJson.get("severity").toString());
        }
    }

    @Override
    public MetricType type() {
        return CVE;
    }

    @Override
    public double compute() {
        return score;
    }

    private double getSeverityCoef(String severity) {
        return switch (severity) {
            case "LOW" -> 2;
            case "MODERATE" -> 3;
            case "HIGH" -> 5;
            case "CRITICAL" -> 8;
            default -> 2;
        };
    }
}
