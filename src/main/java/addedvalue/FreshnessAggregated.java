package addedvalue;

import org.json.simple.JSONObject;

public class FreshnessAggregated extends Freshness{
    public FreshnessAggregated(JSONObject nodeJsonObject) {
        super(nodeJsonObject);
    }

    @Override
    public AddedValueEnum getAddedValueEnum(){
        return AddedValueEnum.FRESHNESS_AGGREGATED;
    }
}
