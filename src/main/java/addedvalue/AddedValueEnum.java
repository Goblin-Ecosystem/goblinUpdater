package addedvalue;

import graph.entities.nodes.NodeType;

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

    public boolean isAggregated(){
        return switch (this.name()) {
            case "CVE", "FRESHNESS"-> false;
            case "CVE_AGGREGATED", "FRESHNESS_AGGREGATED" -> true;
        };
    }

    public AddedValueEnum aggregatedVersion(){
        return switch (this.name()) {
            case "CVE"-> CVE_AGGREGATED;
            case "FRESHNESS" -> FRESHNESS_AGGREGATED;
            default -> this;
        };
    }

    public String getJsonKey(){
        return this.name().toLowerCase();
    }

}
