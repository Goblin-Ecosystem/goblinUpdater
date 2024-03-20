package graph.entities.nodes;

import java.util.Set;

import addedvalue.AddedValueEnum;
import static addedvalue.AddedValueEnum.*;
import updater.preferences.UpdatePreferences;

public class ReleaseNode extends AbstractNode {

    private static final Set<AddedValueEnum> KNOWN_VALUES = Set.of(CVE, CVE_AGGREGATED, FRESHNESS, FRESHNESS_AGGREGATED);

    private double changeCost = 0.0;

    public ReleaseNode(String id) {
        super(id);
    }

    public double getChangeCost() {
        return changeCost;
    }

    public void setChangeCost(double changeCost) {
        this.changeCost = changeCost;
    }

    public String getGa() {
        String[] splitedGav = id().split(":");
        return splitedGav[0] + ":" + splitedGav[1];
    }

    public boolean dominates(ReleaseNode other, UpdatePreferences updatePreferences) {
        return this.getNodeQuality(updatePreferences) <= other.getNodeQuality(updatePreferences)
                && this.changeCost <= other.changeCost
                && (this.getNodeQuality(updatePreferences) < other.getNodeQuality(updatePreferences)
                        || this.changeCost < other.changeCost);
    }

    @Override
    public boolean isRelease() {
        return true;
    }

    @Override
    public boolean isArtifact() {
        return false;
    }

    @Override
    public Set<AddedValueEnum> knownValues() {
        return KNOWN_VALUES;
    }
}
