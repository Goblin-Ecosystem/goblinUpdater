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
            case "LOW" -> 2;
            case "MODERATE" -> 3;
            case "HIGH" -> 5;
            case "CRITICAL" -> 8;
            default -> 2;
        };
    }
}
