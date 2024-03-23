package oldupdater.lpla;

import graph.generator.LPLAGraphGenerator;
import updater.impl.updater.process.graphbased.AbstractUpdater;

public class MavenLPLAUpdater extends AbstractUpdater {
    public MavenLPLAUpdater() {
        super(new LPLAGraphGenerator(), new LPLAUpdateSolver(), new LPLAProjectUpdater());
    }
}
