package updater.impl.process.graphbased.lpga;

import updater.impl.process.graphbased.AbstractUpdater;

public class LPGAUpdater extends AbstractUpdater {

    public LPGAUpdater() {
        super(new LPGAGraphGenerator(), new LPGAUpdateSolver(), new LPGAProjectUpdater());
    }
}
