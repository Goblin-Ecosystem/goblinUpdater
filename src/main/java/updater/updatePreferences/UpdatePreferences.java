package updater.updatePreferences;

import addedvalue.AddedValueEnum;

import java.util.Set;

public interface UpdatePreferences {
    Set<AddedValueEnum> getAddedValueEnumSet();
    double getAddedValueCoef(AddedValueEnum addedValueEnum);
}
