package addedvalue;

public enum AddedValueEnum {
    CVE,
    FRESHNESS;


    public Class<? extends AddedValue> getAddedValueClass(){
        return switch (this.name()) {
            case "CVE" -> Cve.class;
            case "FRESHNESS" -> Freshness.class;
            default -> null;
        };
    }

    public String getJsonKey(){
        return this.name().toLowerCase();
    }
}
