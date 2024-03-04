package graph.entities.nodes;

import addedvalue.AddedValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class NodeObject {
    private final String id;
    private final NodeType type;
    private final List<AddedValue> addedValues = new ArrayList<>();

    public NodeObject(String id, NodeType type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public NodeType getType() {
        return type;
    }

    public void addAddedValue(AddedValue addedValue){
        this.addedValues.add(addedValue);
    }

    public double getNodeQuality(){
        double quality = 0.0;
        for(AddedValue addedValue : addedValues){
            quality += addedValue.getQualityScore();
        }
        return quality;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeObject that = (NodeObject) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
