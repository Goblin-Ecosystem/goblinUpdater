package updater.impl.graph.nodes;

import java.util.Set;

import updater.api.metrics.MetricType;

import java.util.Map;

public class ArtifactNode extends AbstractNode {

    private static final Set<MetricType> KNOWN_VALUES = Set.of();

    /**
     * Constructor for artifact nodes. Assumes the id is of the form "g:a".
     * @param id the id of the node
     */
    public ArtifactNode(String id, Map<MetricType, Double> metricMap) {
        super(id, metricMap);
    }

    private static final boolean isValidId(String id) {
        return id.split(":").length == 2; // FIXME: check more thoroughly
    }

    @Override
    public boolean hasValidId(String id) {
        return ArtifactNode.isValidId(id);
    }

    @Override
    public boolean isRelease() {
        return false;
    }

    @Override
    public boolean isArtifact() {
        return true;
    }

    @Override
    public Set<MetricType> knownValues() {
        return KNOWN_VALUES;
    }
}
