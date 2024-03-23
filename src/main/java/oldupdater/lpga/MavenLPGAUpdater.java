package oldupdater.lpga;

import graph.generator.LPGAGraphGenerator;
import updater.impl.updater.process.graphbased.AbstractUpdater;

/*
 LPGA (Local Possible, Global Analysis)
 moi LPLA (Local Possible, Local Analysis)
 tous les possibles GPGA (Global Possible, Global Analysis)
 */
public class MavenLPGAUpdater extends AbstractUpdater {

    public MavenLPGAUpdater() {
        super(new LPGAGraphGenerator(), new LPGAUpdateSolver(), new LPGAProjectUpdater());
    }
}
