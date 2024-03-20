package graph.structures;

/**
 * Interface for objects that can be identified by a unique identifier. Methods equals and hashCode are to be used on objects of type T so must be redefined if needed.
 */
public interface Identifiable<T> {
    T id();
}
