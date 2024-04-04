package updater.impl.graph.structure.nodes;

import java.util.Set;

import static updater.impl.metrics.SimpleMetricType.*;

import java.util.Map;

import updater.api.metrics.MetricType;
import util.api.CustomGraph;

public class ReleaseNode extends AbstractNode {

    private static final Set<MetricType> KNOWN_VALUES = Set.of(CVE, CVE_AGGREGATED, FRESHNESS, FRESHNESS_AGGREGATED, POPULARITY_1_YEAR, POPULARITY_1_YEAR_AGGREGATED);

    /**
     * Constructor for release nodes. Assumes the id is of the form "g:a:v".
     * @param id the id of the node
     */
    public ReleaseNode(String id, Map<MetricType, Double> metricMap) {
        super(id, metricMap);
    }

    private static final boolean isValidId(String id) {
        if(id.equals(CustomGraph.ROOT_ID)){
            return true;
        }
        return id.split(":").length == 3; // FIXME: check more thoroughly
    }

    @Override
    public boolean hasValidId(String id) {
        return ReleaseNode.isValidId(id);
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
    public Set<MetricType> knownValues() {
        return KNOWN_VALUES;
    }
}
