package updater.impl.updater.process.graphbased.lpla;

import updater.impl.updater.process.graphbased.AbstractUpdater;

public class LPLAUpdater extends AbstractUpdater {

    public LPLAUpdater() {
        super(new LPLAGraphGenerator(), new LPLAUpdateSolver(), new LPLAProjectUpdater());
    }
}
