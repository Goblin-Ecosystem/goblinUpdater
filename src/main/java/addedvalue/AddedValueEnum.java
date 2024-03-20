package addedvalue;

public enum AddedValueEnum {
    CVE,
    CVE_AGGREGATED,
    FRESHNESS,
    FRESHNESS_AGGREGATED;

    public Class<? extends AddedValue> getAddedValueClass() {
        return switch (this) {
            case CVE -> Cve.class;
            case CVE_AGGREGATED -> CveAggregated.class;
            case FRESHNESS -> Freshness.class;
            case FRESHNESS_AGGREGATED -> FreshnessAggregated.class;
            default -> null;
        };
    }

    public boolean isAggregated() {
        return switch (this) {
            case CVE_AGGREGATED, FRESHNESS_AGGREGATED -> true;
            default -> false;
        };
    }

    public AddedValueEnum aggregatedVersion() {
        return switch (this) {
            case CVE -> CVE_AGGREGATED;
            case FRESHNESS -> FRESHNESS_AGGREGATED;
            default -> this;
        };
    }

    public AddedValueEnum notAggregatedVersion() {
        return switch (this) {
            case CVE_AGGREGATED -> CVE;
            case FRESHNESS_AGGREGATED -> FRESHNESS;
            default -> this;
        };
    }

    public String getJsonKey() {
        return this.name().toLowerCase();
    }

}
