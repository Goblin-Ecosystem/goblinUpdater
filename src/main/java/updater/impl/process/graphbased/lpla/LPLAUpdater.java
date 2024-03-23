package updater.impl.process.graphbased.lpla;

import updater.impl.process.graphbased.AbstractUpdater;

public class LPLAUpdater extends AbstractUpdater {

    public LPLAUpdater() {
        super(new LPLAGraphGenerator(), new LPLAUpdateSolver(), new LPLAProjectUpdater());
    }
}
