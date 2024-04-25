package updater.helpers;

import japicmp.cmp.JApiCmpArchive;
import japicmp.cmp.JarArchiveComparator;
import japicmp.cmp.JarArchiveComparatorOptions;
import japicmp.model.JApiChangeStatus;
import japicmp.model.JApiClass;
import updater.api.graph.structure.UpdateNode;
import util.helpers.maven.MavenLocalRepository;

import java.io.File;
import java.util.List;

public class JapicmpHelpers {

    private JapicmpHelpers(){}

    public static double computeChangeCost(UpdateNode oldRelease, UpdateNode newRelease) {
        if (oldRelease.equals(newRelease)) {
            return 0.0;
        }
        String[] oldReleaseSplited = oldRelease.id().split(":");
        String[] newReleaseSplited = oldRelease.id().split(":");
        if (oldReleaseSplited.length < 3 || newReleaseSplited.length < 3 ){
            return 0.0;
        }
        JarArchiveComparatorOptions comparatorOptions = new JarArchiveComparatorOptions();
        JarArchiveComparator jarArchiveComparator = new JarArchiveComparator(comparatorOptions);
        MavenLocalRepository mavenLocalRepository = MavenLocalRepository.getInstance();

        JApiCmpArchive oldArchive = new JApiCmpArchive(new File(mavenLocalRepository.downloadArtifact(oldRelease.id())), oldReleaseSplited[2]);
        JApiCmpArchive newArchive = new JApiCmpArchive(new File(mavenLocalRepository.downloadArtifact(newRelease.id())), newReleaseSplited[2]);

        List<JApiClass> jApiClasses = jarArchiveComparator.compare(oldArchive, newArchive);
        return jApiClasses.stream()
                .filter(jApiClass -> {
                    JApiChangeStatus status = jApiClass.getChangeStatus();
                    return status == JApiChangeStatus.MODIFIED || status == JApiChangeStatus.REMOVED;
                })
                .count();
    }
}
