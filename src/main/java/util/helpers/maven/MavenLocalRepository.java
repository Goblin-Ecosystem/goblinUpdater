package util.helpers.maven;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

import util.helpers.system.FileHelpers;
import util.helpers.system.LoggerHelpers;

public class MavenLocalRepository {
    private static MavenLocalRepository instance;
    private static final String LOCAL_REPO_PATH = "workspace";
    private final RemoteRepository centralRepo = new RemoteRepository.Builder("central", "default",
            "https://repo1.maven.org/maven2/").build();
    private final DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
    private final RepositorySystem system = newRepositorySystem(locator);
    private final RepositorySystemSession session = newSession(system);

    private MavenLocalRepository() {
        FileHelpers.createDirectory(LOCAL_REPO_PATH);
    }

    public static synchronized MavenLocalRepository getInstance() {
        if (instance == null) {
            instance = new MavenLocalRepository();
        }
        return instance;
    }

    public void clearLocalRepo() {
        FileHelpers.deleteDirectoryIfExist(LOCAL_REPO_PATH);
        FileHelpers.createDirectory(LOCAL_REPO_PATH);
    }

    private RepositorySystem newRepositorySystem(DefaultServiceLocator locator) {
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        return locator.getService(RepositorySystem.class);
    }

    private RepositorySystemSession newSession(RepositorySystem system) {
        // FIXME: session hides field at line 26.
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        // TODO: local or .m2 ?
        LocalRepository localRepo = new LocalRepository(LOCAL_REPO_PATH);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        return session;
    }

    public String downloadArtifact(String artifactId) {
        Artifact artifact = new DefaultArtifact(artifactId);

        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(artifact);
        request.addRepository(centralRepo);

        try {
            ArtifactResult result = system.resolveArtifact(session, request);
            return result.getArtifact().getFile().getPath();
        } catch (ArtifactResolutionException e) {
            LoggerHelpers.warning(e.getMessage());
            return null;
        }
    }
}
