package bazarRefonte;

import java.util.List;

import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

public class OrLP {

    public static void printSolution(MPSolver problem) {
        System.out.println("Solution:");
        MPVariable[] variables = problem.variables();
        for (int i = 0; i < variables.length; ++i) {
            System.out.println(variables[i].name() + " : " + variables[i].solutionValue());
        }
        System.out.println("Computed in " + problem.wallTime() + " ms");
    }

    public static void printProblem(MPSolver problem) {
        System.out.println("Problem:");
        MPVariable[] variables = problem.variables();
        for (int i = 0; i < variables.length; ++i) {
            System.out.println(variables[i].name());
        }
        MPConstraint[] constraints = problem.constraints();
        for (int i = 0; i < constraints.length; ++i) {
            System.out.println(constraints[i].name());
        }
    }

    // create constraint name: x = v
    public static void makeEqualityConstraint(MPSolver solver, String name, MPVariable x, int v) {
        MPConstraint constraint = solver.makeConstraint(v, v, name);
        constraint.setCoefficient(x, 1);
    }

    // create constraint name: x = y
    public static void makeEqualityConstraint(MPSolver solver, String name, MPVariable x, MPVariable y) {
        MPConstraint constraint = solver.makeConstraint(0, 0, name);
        constraint.setCoefficient(x, 1);
        constraint.setCoefficient(y, -1);
    }

    // create constraint name: x >= y
    public static void makeSupEqualConstraint(MPSolver solver, String name, MPVariable x, MPVariable y) {
        MPConstraint constraint = solver.makeConstraint(0, MPSolver.infinity(), name);
        constraint.setCoefficient(x, 1);
        constraint.setCoefficient(y, -1);
    }

    // create constraint name: sum(xs) = k*y
    public static void makeEqualityWithSumConstraint(MPSolver solver, String name, List<MPVariable> xs,
                                                     int k, MPVariable y) {
        MPConstraint constraint = solver.makeConstraint(0, 0, name);
        xs.forEach(x -> constraint.setCoefficient(x, 1));
        constraint.setCoefficient(y, -k);
    }

    // create constraint name: sum(xs) >= y
    public static void makeSupEqualWithSumConstraint(MPSolver solver, String name, List<MPVariable> xs,
                                                     MPVariable y) {
        MPConstraint constraint = solver.makeConstraint(0, MPSolver.infinity(), name);
        xs.forEach(x -> constraint.setCoefficient(x, 1));
        constraint.setCoefficient(y, -1);
    }

}

