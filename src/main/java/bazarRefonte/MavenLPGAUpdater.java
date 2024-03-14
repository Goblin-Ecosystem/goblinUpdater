package bazarRefonte;

import com.google.ortools.linearsolver.MPSolver;
import org.checkerframework.checker.units.qual.A;

/*
 LPGA (Local Possible, Global Analysis)
 moi LPLA (Local Possible, Local Analysis)
 tous les possibles GPGA (Global Possible, Global Analysis)
 */
public class MavenLPGAUpdater extends AbstractUpdater {

    protected MavenLPGAUpdater() {
        super(new LPGAGraphGenerator(), new LPGAUpdateSolver(), new LPGAProjectUpdater());
    }
}
