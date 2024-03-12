package code.sandbox;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPSolver;

import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.util.ArrayList;
import java.util.List;

public final class OrTools {
    public static void main(String[] args) {
        Loader.loadNativeLibraries();

        MPSolver solver = MPSolver.createSolver("GLOP");

        // CREATION DU DOMAINE

        final int LIBRARIES = 1000;
        final int VERSION_PER_LIBRARY = 10;

        List<String> libraries = new ArrayList<>();
        List<String> artifacts = new ArrayList<>();
        List<Tuple2<String, String>> versions = new ArrayList<>();

        for (int i = 1; i <= LIBRARIES; ++i) {
            String libname = "x" + i;  // library i
            libraries.add(libname);
            for (int j = 1; j <= VERSION_PER_LIBRARY; ++j) {
                String versionname = "v" + i + "_" + j; // library i version j
                artifacts.add(versionname);
                versions.add(Tuple.of(libname, versionname));
            }
        }

        // CREATION DU PROBLEME

        // a variable for each library and artifact
        libraries.stream().forEach(l -> solver.makeNumVar(0.0, 1.0, l));
        artifacts.stream().forEach(a -> solver.makeNumVar(0.0, 1.0, a));


        System.out.println("Number of variables = " + solver.numVariables());

        // une version par librairie


        // // [START constraints]
        // // Create a linear constraint, 0 <= x + y <= 2.
        // MPConstraint ct = solver.makeConstraint(0.0, 2.0, "ct");
        // ct.setCoefficient(x, 1);
        // ct.setCoefficient(y, 1);

        // System.out.println("Number of constraints = " + solver.numConstraints());
        // // [END constraints]

        // // [START objective]
        // // Create the objective function, 3 * x + y.
        // MPObjective objective = solver.objective();
        // objective.setCoefficient(x, 3);
        // objective.setCoefficient(y, 1);
        // objective.setMaximization();
        // // [END objective]

        // // [START solve]
        // solver.solve();
        // // [END solve]
        // // [START print_solution]
        // System.out.println("Solution:");
        // System.out.println("Objective value = " + objective.value());
        // System.out.println("x = " + x.solutionValue());
        // System.out.println("y = " + y.solutionValue());
        // // [END print_solution]
    }

    private OrTools() {
    }
}
// [END program]
