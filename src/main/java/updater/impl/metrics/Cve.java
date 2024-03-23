package updater.impl.metrics;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import updater.api.metrics.Metric;
import updater.api.metrics.MetricType;

public class Cve implements Metric {
    private final JSONArray valueJsonArray;

    public Cve(JSONObject nodeJsonObject) {
        this.valueJsonArray = (JSONArray) nodeJsonObject.get(type().getJsonKey());
    }

    @Override
    public MetricType type() {
        return MetricType.CVE;
    }

    @Override
    public double compute() {
        double score = 0;
        for (Object cve : valueJsonArray) {
            JSONObject cveJson = (JSONObject) cve;
            score += 1 * getSeverityCoef(cveJson.get("severity").toString());
        }
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
