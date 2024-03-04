package addedvalue;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Cve implements AddedValue{
    JSONArray valueJsonArray;

    public Cve(JSONObject nodeJsonObject) {
        this.valueJsonArray = (JSONArray) nodeJsonObject.get(getAddedValueEnum().getJsonKey());
    }

    @Override
    public AddedValueEnum getAddedValueEnum(){
        return AddedValueEnum.CVE;
    }

    @Override
    public double getQualityScore(){
        double score = 0;
        for(Object cve : valueJsonArray){
            JSONObject cveJson = (JSONObject) cve;
            score += 1 * getSeverityCoef(cveJson.get("severity").toString());
        }
        return score;
    }

    private double getSeverityCoef(String severity) {
        return switch (severity) {
            case "LOW" -> 0.25;
            case "MODERATE" -> 0.5;
            case "HIGH" -> 0.75;
            case "CRITICAL" -> 1;
            default -> 0;
        };
    }
}
