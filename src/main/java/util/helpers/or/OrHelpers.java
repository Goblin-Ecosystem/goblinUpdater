package util.helpers.or;

import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import java.util.List;

import io.vavr.Tuple2;
import util.helpers.system.LoggerHelpers;

// TODO: some DRY cleaning is possible
public class OrHelpers {

    public static void printTime(MPSolver problem) {
        LoggerHelpers.instance().info("-- Computed in " + problem.wallTime() + " ms");
    }

    public static void printSolution(MPSolver problem, boolean onlyNonZero) {
        LoggerHelpers.instance().info("## Solution:");
        MPVariable[] variables = problem.variables();
        for (int i = 0; i < variables.length; ++i) {
            double value = variables[i].solutionValue();
            if (onlyNonZero && value == 0) continue;
            LoggerHelpers.instance().info(variables[i].name() + " : " + variables[i].solutionValue());
        }
    }

    public static void printProblem(MPSolver problem) {
        LoggerHelpers.instance().low("## Problem:");
        LoggerHelpers.instance().low(problem.exportModelAsLpFormat());
    }

    // create constraint name: x = v
    public static void x_eq_v(MPSolver solver, String name, MPVariable x, int v) {
        MPConstraint constraint = solver.makeConstraint(v, v, name);
        constraint.setCoefficient(x, 1);
    }

    // create constraint name: x = y
    public static void x_eq_y(MPSolver solver, String name, MPVariable x, MPVariable y) {
        MPConstraint constraint = solver.makeConstraint(0, 0, name);
        constraint.setCoefficient(x, 1);
        constraint.setCoefficient(y, -1);
    }

    // create constraint name: x >= y
    public static void x_ge_y(MPSolver solver, String name, MPVariable x, MPVariable y) {
        MPConstraint constraint = solver.makeConstraint(0, MPSolver.infinity(), name);
        constraint.setCoefficient(x, 1);
        constraint.setCoefficient(y, -1);
    }

    // create constraint name: sum(xi) = k*y
    // i.e., sum(xi) - k*y = 0
    public static void sum_xi_eq_k_times_y(MPSolver solver, String name, List<MPVariable> xs,
            int k, MPVariable y) {
        MPConstraint constraint = solver.makeConstraint(0, 0, name);
        xs.forEach(x -> constraint.setCoefficient(x, 1));
        constraint.setCoefficient(y, -k);
    }

    // create constraint name: sum(ki*xi) = k*y
    // i.e., sum(ki*xi) - k*y = 0
    public static void sum_ki_times_xi_eq_k_times_y(MPSolver solver, String name,
            List<Tuple2<MPVariable, Double>> ts,
            Double k, MPVariable y) {
        MPConstraint constraint = solver.makeConstraint(0, 0, name);
        ts.forEach(t -> constraint.setCoefficient(t._1(), t._2()));
        constraint.setCoefficient(y, -k);
    }

    // create constraint name: sum(xs) >= y + n
    // i.e., sum(xs) - y >= n
    public static void sum_xi_ge_y_plus_n(MPSolver solver, String name, List<MPVariable> xs,
            MPVariable y, Double n) {
        MPConstraint constraint = solver.makeConstraint(n, MPSolver.infinity(), name);
        xs.forEach(x -> constraint.setCoefficient(x, 1));
        constraint.setCoefficient(y, -1);
    }

    // create constraint name: sum(ki*xi) >= y + n
    // i.e., sum(ki*xi) - y >= n
    public static void sum_ki_times_xi_ge_y_plus_n(MPSolver solver, String name,
            List<Tuple2<MPVariable, Double>> ts, MPVariable y, Double n) {
        MPConstraint constraint = solver.makeConstraint(n, MPSolver.infinity(), name);
        ts.forEach(t -> constraint.setCoefficient(t._1(), t._2()));
        constraint.setCoefficient(y, -1);
    }

    // create constraint name: y >= sum(ki*xi) + n
    // i.e., y - sum(ki*xi) >= n
    public static void y_ge_sum_ki_times_xi_plus_n(MPSolver solver, String name,
            MPVariable y, List<Tuple2<MPVariable, Double>> ts, Double n) {
        MPConstraint constraint = solver.makeConstraint(n, MPSolver.infinity(), name);
        ts.forEach(t -> constraint.setCoefficient(t._1(), -t._2()));
        constraint.setCoefficient(y, 1);
    }

}
