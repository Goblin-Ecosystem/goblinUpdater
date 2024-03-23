package updater.impl.updater.process.graphbased.lpga;

import updater.impl.updater.process.graphbased.AbstractUpdater;

public class LPGAUpdater extends AbstractUpdater {

    public LPGAUpdater() {
        super(new LPGAGraphGenerator(), new LPGAUpdateSolver(), new LPGAProjectUpdater());
    }
}
