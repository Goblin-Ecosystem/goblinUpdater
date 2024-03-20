package addedvalue;

import org.json.simple.JSONObject;

public class CveAggregated extends Cve {
    public CveAggregated(JSONObject nodeJsonObject) {
        super(nodeJsonObject);
    }

    @Override
    public AddedValueEnum getAddedValueEnum() {
        return AddedValueEnum.CVE_AGGREGATED;
    }
}
