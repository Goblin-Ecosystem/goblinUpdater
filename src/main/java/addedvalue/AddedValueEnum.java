package addedvalue;

import graph.entities.nodes.NodeType;
import util.YmlConfReader;

public enum AddedValueEnum {
    CVE,
    CVE_AGGREGATED,
    FRESHNESS,
    FRESHNESS_AGGREGATED;


    public Class<? extends AddedValue> getAddedValueClass(){
        return switch (this.name()) {
            case "CVE" -> Cve.class;
            case "CVE_AGGREGATED" -> CveAggregated.class;
            case "FRESHNESS" -> Freshness.class;
            case "FRESHNESS_AGGREGATED" -> FreshnessAggregated.class;
            default -> null;
        };
    }

    public NodeType getTargetNodeType(){
        return switch (this.name()) {
            case "CVE", "CVE_AGGREGATED", "FRESHNESS", "FRESHNESS_AGGREGATED" -> NodeType.RELEASE;
            default -> null;
        };
    }

    public String getJsonKey(){
        return this.name().toLowerCase();
    }

    public double getCoef(){
        return YmlConfReader.getInstance().getAddedValueCoef(this);
    }
}
