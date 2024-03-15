package updater;

import graph.structures.CustomGraph;
import project.Project;

public interface ProjectUpdater {
    Project updateProject(Project project, CustomGraph initialGraph, CustomGraph updatedGraph);
}
