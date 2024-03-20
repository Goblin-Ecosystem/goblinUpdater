package updater.preferences;

import addedvalue.AddedValueEnum;

import java.util.Set;

public interface UpdatePreferences {
    Set<AddedValueEnum> getAddedValueEnumSet();

    Set<AddedValueEnum> getAggregatedAddedValueEnumSet();

    double getAddedValueCoef(AddedValueEnum addedValueEnum);
}
