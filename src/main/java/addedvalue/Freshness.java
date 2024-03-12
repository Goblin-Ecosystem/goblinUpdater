package addedvalue;

import org.json.simple.JSONObject;

public class Freshness implements AddedValue{
    private final JSONObject valueJsonObject;

    public Freshness(JSONObject nodeJsonObject) {
        this.valueJsonObject = (JSONObject) nodeJsonObject.get(getAddedValueEnum().getJsonKey());
    }

    @Override
    public AddedValueEnum getAddedValueEnum(){
        return AddedValueEnum.FRESHNESS;
    }

    @Override
    public double getQualityScore(){
        return Double.parseDouble(valueJsonObject.get("numberMissedRelease").toString());
    }
}
