package util;

import com.github.maracas.LibraryJar;
import com.github.maracas.Maracas;
import com.github.maracas.SourcesDirectory;
import com.github.maracas.brokenuse.BrokenUse;
import com.github.maracas.brokenuse.DeltaImpact;
import com.github.maracas.delta.Delta;
import graph.entities.nodes.NodeObject;
import graph.entities.nodes.ReleaseNode;

import java.nio.file.Path;
import java.util.Set;

public class MaracasHelpers {

    public static double computeChangeCost(Path projectPath, NodeObject currentRelease, ReleaseNode artifactRelease){
        try {
            Maracas maracas = new Maracas();
            // Setting up the library versions and clients
            MavenLocalRepository mavenLocalRepository = MavenLocalRepository.getInstance();
            LibraryJar v1 = LibraryJar.withoutSources(Path.of(mavenLocalRepository.downloadArtifact(currentRelease.getId())));
            LibraryJar v2 = LibraryJar.withoutSources(Path.of(mavenLocalRepository.downloadArtifact(artifactRelease.getId())));
            SourcesDirectory client = SourcesDirectory.of(projectPath);

            Delta delta = maracas.computeDelta(v1, v2);
            DeltaImpact deltaImpact = maracas.computeDeltaImpact(client, delta);
            Set<BrokenUse> brokenUses = deltaImpact.brokenUses();
            //TODO fonction d'estimation de coût
            return brokenUses.size();
        }catch (Exception e){
            LoggerHelpers.error("Maracas fail:\n"+e.getMessage());
            return 9999999.0;
        }
    }
}
