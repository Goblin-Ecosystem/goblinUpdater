package updater.api.project;

/**
 * A simple representation of a dependency, with three parts: groupId, artifactId, and version.
 */
public record Dependency(String groupId, String artifactId, String version) {
    /**
     * for free:
     * - private final fields groupId, artifactId, version
     * - public accessors groupId(), artifactId(), version()
     * - public canonical constructor with 3 parameters
     * - public equals and hashCode methods
     * - public toString method
     */
    public String getGav(){
        return groupId+":"+artifactId+":"+version;
    }

    public String getGa(){
        return groupId+":"+artifactId;
    }
}
