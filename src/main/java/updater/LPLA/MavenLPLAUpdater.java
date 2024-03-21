package updater.LPLA;

import graph.generator.LPLAGraphGenerator;
import updater.AbstractUpdater;

public class MavenLPLAUpdater extends AbstractUpdater {
    public MavenLPLAUpdater() {
        super(new LPLAGraphGenerator(), new LPLAUpdateSolver(), new LPLAProjectUpdater());
    }
}
