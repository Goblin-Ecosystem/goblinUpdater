package graph.entities.nodes;

import java.util.Set;
import java.util.Map;

import addedvalue.AddedValueEnum;

import static addedvalue.AddedValueEnum.*;
import updater.preferences.UpdatePreferences;

public class ReleaseNode extends AbstractNode {

    private static final Set<AddedValueEnum> KNOWN_VALUES = Set.of(CVE, CVE_AGGREGATED, FRESHNESS, FRESHNESS_AGGREGATED);

    private double changeCost = 0.0;

    /**
     * Constructor for release nodes. Assumes the id is of the form "g:a:v".
     * @param id the id of the node
     */
    public ReleaseNode(String id, Map<AddedValueEnum, Double> metricMap) {
        super(id, metricMap);
    }

    private static final boolean isValidId(String id) {
        return id.split(":").length == 3; // FIXME: check more thoroughly
    }

    @Override
    public boolean hasValidId(String id) {
        return ReleaseNode.isValidId(id);
    }

    // FIXME: should be private or in an interface
    public double getChangeCost() {
        return changeCost;
    }

    // FIXME: should be private or in an interface
    public void setChangeCost(double changeCost) {
        this.changeCost = changeCost;
    }
    
    // FIXME: should be private or in an interface
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
