package addedvalue;

/**
 * Enum for added value types. Adding a new enum value will require adding:
 * <OL>
 * <LI>A new enum value to the enum
 * <LI>A new class that implements AddedValue and update getAddedValueClass
 * accordingly
 * <LI>Update the code for isAggregated, aggregatedVersion, and
 * notAggregatedVersion accordingly
 * </OL>
 * This should be done in concordance with Goblin's Weaver code.
 */
public enum AddedValueEnum {
    CVE,
    CVE_AGGREGATED,
    FRESHNESS,
    FRESHNESS_AGGREGATED;

    /**
     * Returns the class that implements AddedValue for this enum value
     */
    public Class<? extends AddedValue> getAddedValueClass() {
        return switch (this) {
            case CVE -> Cve.class;
            case CVE_AGGREGATED -> CveAggregated.class;
            case FRESHNESS -> Freshness.class;
            case FRESHNESS_AGGREGATED -> FreshnessAggregated.class;
            default -> null;
        };
    }

    /**
     * Returns true if the enum value corresponds to an aggregated value, false
     * otherwise
     */
    public boolean isAggregated() {
        return switch (this) {
            case CVE_AGGREGATED, FRESHNESS_AGGREGATED -> true;
            default -> false;
        };
    }

    /**
     * Returns the enum value corresponding to the aggregated version of this enum
     * value, or this enum value if it is not an aggregated value
     */
    public AddedValueEnum aggregatedVersion() {
        return switch (this) {
            case CVE -> CVE_AGGREGATED;
            case FRESHNESS -> FRESHNESS_AGGREGATED;
            default -> this;
        };
    }

    /**
     * Returns the enum value corresponding to the non-aggregated version of this
     * enum value, or this enum value if it is not an aggregated value
     */
    public AddedValueEnum notAggregatedVersion() {
        return switch (this) {
            case CVE_AGGREGATED -> CVE;
            case FRESHNESS_AGGREGATED -> FRESHNESS;
            default -> this;
        };
    }

    /**
     * Returns the key used to get the value from JSON, e.g. "cve" for CVE and
     * "freshness" for FRESHNESS
     */
    public String getJsonKey() {
        return this.name().toLowerCase();
    }

}
