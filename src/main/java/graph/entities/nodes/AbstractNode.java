package graph.entities.nodes;

import addedvalue.AddedValue;
import updater.preferences.UpdatePreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractNode implements UpdateNode {
    private final String id;
    private final List<AddedValue> addedValues = new ArrayList<>();
    private Double quality = null;

    protected AbstractNode(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }

    public void addAddedValue(AddedValue addedValue) {
        this.addedValues.add(addedValue);
    }

    public double getNodeQuality(UpdatePreferences updatePreferences) {
        if (this.quality != null) {
            return this.quality;
        }
        this.quality = 0.0;
        for (AddedValue addedValue : addedValues) {
            this.quality += (addedValue.getQualityScore()
                    * updatePreferences.getAddedValueCoef(addedValue.getAddedValueEnum())); // TODO normalize quality
                                                                                            // score
        }
        return this.quality;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AbstractNode that = (AbstractNode) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
