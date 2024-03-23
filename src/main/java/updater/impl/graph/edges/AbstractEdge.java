package updater.impl.graph.edges;

import java.util.Optional;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

import org.jgrapht.graph.DefaultEdge;

import updater.api.metrics.MetricContainer;
import updater.api.metrics.MetricType;
import updater.impl.metrics.MetricMap;

public abstract class AbstractEdge extends DefaultEdge implements UpdateEdge {

    private String id;
    // FIXME: not DRY wrt AbstractNode, can be extracted in a AbstractGraphElementWithMetricContainer.
    private MetricContainer<MetricType> metricMap;

    protected AbstractEdge(String id, Map<MetricType, Double> metricMap) {
        super();
        this.id = id;
        if (metricMap!= null)
            this.metricMap = new MetricMap<>(metricMap);
        else
            this.metricMap = new MetricMap<>(new HashMap<>());
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public Set<MetricType> contentTypes() {
        return metricMap.contentTypes();
    }

    @Override
    public void put(MetricType m, Double value) {
        metricMap.put(m, value);
    }

    @Override
    public Optional<Double> get(MetricType m) {
        return metricMap.get(m);
    }

    @Override
    public String targetVersion() {
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractEdge other = (AbstractEdge) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
