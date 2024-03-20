package addedvalue;

/**
 * Interface for values associated to elements in dependency update
 */
public interface AddedValue {
    /**
     * Returns the enum value of this AddedValue object.
     */
    AddedValueEnum getAddedValueEnum();

    /**
     * Returns the quality score of this AddedValue object.
     */
    double getQualityScore();
}
