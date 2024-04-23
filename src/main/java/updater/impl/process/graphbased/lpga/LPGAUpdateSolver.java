package updater.impl.process.graphbased.lpga;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;
import updater.api.metrics.MetricType;
import updater.api.preferences.Constraint;
import updater.api.preferences.Preferences;
import updater.api.process.graphbased.UpdateSolver;
import updater.impl.preferences.AbsenceConstraint;
import updater.impl.preferences.CostLimitConstraint;
import updater.impl.preferences.CveLimitConstraint;
import updater.impl.preferences.PresenceConstraint;
import util.helpers.or.OrHelpers;
import util.helpers.system.LoggerHelpers;

import java.util.*;
import java.util.function.Function;

public class LPGAUpdateSolver implements UpdateSolver {

        public LPGAUpdateSolver() {
        }

        @Override
        public Optional<UpdateGraph<UpdateNode, UpdateEdge>> resolve(UpdateGraph<UpdateNode, UpdateEdge> updateGraph,
                        Preferences updatePreferences) {
                Loader.loadNativeLibraries();
                MPSolver problem = createProblem(updateGraph, updatePreferences);
                integrateConstraints(updateGraph, updatePreferences, problem);
                return solveProblem(problem, updateGraph, updatePreferences)
                                .flatMap(solution -> solutionToGraph(solution, updateGraph, updatePreferences));
        }

        private Optional<UpdateGraph<UpdateNode, UpdateEdge>> solutionToGraph(MPSolver solution,
                        UpdateGraph<UpdateNode, UpdateEdge> updateGraph, Preferences updatePreferences) {
                // TODO: if the graph is needed
                return Optional.empty();
        }

        private Optional<MPSolver> solveProblem(MPSolver problem, UpdateGraph<UpdateNode, UpdateEdge> updateGraph,
                        Preferences updatePreferences) {
                MPSolver.ResultStatus result = problem.solve();
                if (result == MPSolver.ResultStatus.OPTIMAL || result == MPSolver.ResultStatus.FEASIBLE) {
                        LoggerHelpers.instance().info("Problem solved");
                        OrHelpers.printTime(problem);
                        OrHelpers.printProblem(problem);
                        OrHelpers.printSolution(problem, true);
                        return Optional.of(problem);
                } else {
                        LoggerHelpers.instance().info("Problem not solved");
                        OrHelpers.printProblem(problem);
                        return Optional.empty();
                }
        }

        private <N extends UpdateNode, E extends UpdateEdge> void visit(UpdateGraph<N,E> updateGraph, MPSolver problem, AbsenceConstraint ac) {
                N node = updateGraph.nodes(n -> n.id().equals(ac.id())).stream().findFirst().orElse(null);
                if (node != null) {
                        MPVariable v = GraphLP.releaseVariable(problem, node);
                        OrHelpers.x_eq_v(problem, "constraint on absence of " + ac.id(), v , 0);
                } else {
                        LoggerHelpers.instance().warning("Unknown node id: " + ac.id());
                }
        }

        private <N extends UpdateNode, E extends UpdateEdge> void visit(UpdateGraph<N,E> updateGraph, MPSolver problem, PresenceConstraint pc) {
                N node = updateGraph.nodes(n -> n.id().equals(pc.id())).stream().findFirst().orElse(null);
                if (node != null) {
                        MPVariable v = GraphLP.releaseVariable(problem, node);
                        OrHelpers.x_eq_v(problem, "constraint on presence of " + pc.id(), v , 1);
                } else {
                        LoggerHelpers.instance().warning("Unknown node id: " + pc.id());
                }
        }

        private void visit(MPSolver problem, CostLimitConstraint clc) {
                MPVariable v = GraphLP.totalCostVariable(problem);
                if (v != null) {
                        OrHelpers.x_le_v(problem, "constraint on cost limit", v, clc.limit());
                } else {
                        LoggerHelpers.instance().warning("cost limit not used: no cost metric");
                }
        }
        
        private void visit(MPSolver problem, CveLimitConstraint clc) {
                MPVariable v = GraphLP.totalCveVariable(problem);
                if (v != null) {
                        OrHelpers.x_le_v(problem, "constraint on cve limit", v, clc.limit());
                } else {
                        LoggerHelpers.instance().warning("cve limit not used: no cve metric");
                }
        }

        private <N extends UpdateNode, E extends UpdateEdge> void integrateConstraints(
                        UpdateGraph<N, E> updateGraph,
                        Preferences preferences,
                        MPSolver problem) {
                for (Constraint c : preferences.constraints()) {
                        // TODO: avoid instanceof
                        if (c instanceof AbsenceConstraint ac) {
                                visit(updateGraph, problem, ac);
                        } else if (c instanceof PresenceConstraint pc) {
                                visit(updateGraph, problem, pc);
                        } else if (c instanceof CostLimitConstraint clc) {
                                visit(problem, clc);
                        } else if (c instanceof CveLimitConstraint clc) {
                                visit(problem, clc);
                        }
                }
        }

        private <N extends UpdateNode, E extends UpdateEdge> MPSolver createProblem(UpdateGraph<N, E> updateGraph,
                        Preferences updatePreferences) {
                // TODO: select the best solver. CBC, CLP, GLPK, CP-SAT, ... 
                // Taking care with the correct treatment for "binary" variables (that should be 0 or 1 not 0.5 for example)
                // GLOP is not working correctly with binary variables. (all linear solvers?)
                // CBC is good, no setNumThreads still.
                MPSolver solver = MPSolver.createSolver("CBC");
                // TODO: could be enhanced
                Set<N> artifactNodes = updateGraph.artifactNodes();
                Set<N> releaseNodes = updateGraph.releaseNodes();
                Set<E> versionEdges = updateGraph.versionEdges();
                Set<E> dependencyEdges = updateGraph.dependencyEdges();
                Set<E> changeEdges = updateGraph.changeEdges();

                // create a variable for each node in releaseNodes
                releaseNodes.forEach(n -> solver.makeBoolVar(GraphLP.releaseVariableName(n)));

                // create a variable for each node in artifactNodes
                artifactNodes.forEach(n -> solver.makeBoolVar(GraphLP.artifactVariableName(n)));

                // create variable for each change edge
                changeEdges.forEach(e -> solver.makeBoolVar(GraphLP.changeVariableName(updateGraph, e)));

                // create a constraint stating that the variable for the root node equals 1
                // (ROOT)
                updateGraph.rootNode().ifPresent(n -> OrHelpers.x_eq_v(solver,
                                "ROOT",
                                GraphLP.releaseVariable(solver, n),
                                1));

                // create constraints for versions (VER)
                // for all a in artifactNodes, sum_{r in versions(a)} (var_r) = var_a
                // and
                // create constraints for dependencies (DEP 1)
                // for all a in artifactNodes, sum_{r in dependants(a)} (var_r) >= var_a
                for (N a : artifactNodes) {
                        String aName = GraphLP.artifactVariableName(a);
                        MPVariable vA = GraphLP.artifactVariable(solver, a);
                        List<MPVariable> releases = versionEdges.stream()
                                        .filter(e -> updateGraph.source(e) == a)
                                        .map(updateGraph::target)
                                        .map(r -> GraphLP.releaseVariable(solver, r))
                                        .toList();
                        OrHelpers.sum_xi_eq_k_times_y(solver,
                                        "VERSIONS of " + GraphLP.artifactVariableName(a),
                                        releases,
                                        1, vA);
                        List<MPVariable> dependants = dependencyEdges.stream()
                                        .filter(e -> updateGraph.target(e) == a)
                                        .map(updateGraph::source)
                                        .map(r -> GraphLP.releaseVariable(solver, r))
                                        .toList();
                        OrHelpers.sum_xi_ge_y_plus_n(solver, "DEP1 " + aName, dependants, vA, 0.0);
                }

                // create constraints for dependencies (DEP2)
                // for all d=(s,t) in dependencyEdges, var_t >= var_s
                for (E d : dependencyEdges) {
                        N source = updateGraph.source(d);
                        N target = updateGraph.target(d);
                        String sName = GraphLP.releaseVariableName(source);
                        String tName = GraphLP.artifactVariableName(target);
                        MPVariable sVar = GraphLP.releaseVariable(solver, source);
                        MPVariable tVar = GraphLP.artifactVariable(solver, target);
                        OrHelpers.x_ge_y(solver, "DEP2 " + sName + " " + tName, tVar, sVar);
                }
                LoggerHelpers.instance().info(dependencyEdges.size() + " dependency edges");

                // create constraints for possibilities (CHG1, CHG2, CHG3)
                // for each e=(r,r') in changeEdges, e = r*r'
                // using linearization we get e<=r, e<=r' and e>=r+r'-1
                for (E e : changeEdges) {
                        N source = updateGraph.source(e);
                        N target = updateGraph.target(e);
                        String sName = GraphLP.releaseVariableName(source);
                        String tName = GraphLP.releaseVariableName(target);
                        MPVariable sVar = GraphLP.releaseVariable(solver, source);
                        MPVariable tVar = GraphLP.releaseVariable(solver, target);
                        MPVariable eVar = GraphLP.changeVariable(solver, updateGraph, e);
                        OrHelpers.x_ge_y(solver, "CHG2 " + sName + " " + tName, sVar, eVar);
                        OrHelpers.x_ge_y(solver, "CHG1 " + sName + " " + tName, tVar, eVar);
                        OrHelpers.y_ge_sum_ki_times_xi_plus_n(solver, "CHG 3 " + sName + " " + tName,
                                        eVar, List.of(Tuple.of(sVar, 1.0), Tuple.of(tVar, 1.0)), -1.0);
                }
                LoggerHelpers.instance().info(changeEdges.size() + " change edges");

                // TODO: rather fail using Optional or Either, which means problem generation
                // can fail too (not only solving it)
                if (!updatePreferences.isValid()) {
                        throw new IllegalArgumentException("Update preferences are not valid");
                }
                MetricType costMetric = updatePreferences.costMetrics().stream().findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("No cost added value defined"));

                // Aggregative variables
                MPVariable totalQuality = solver.makeNumVar(0.0, Double.POSITIVE_INFINITY, GraphLP.QUALITY_VARIABLE_NAME);
                MPVariable totalCost = solver.makeNumVar(0.0, Double.POSITIVE_INFINITY, GraphLP.COST_VARIABLE_NAME);

                // Quality variables
                // and associated constraints (quality is only on release edges)
                // TODO: check if it is always sum(...) = 1 * totalQuality
                // if there is at least a metric for which it is not the case then a form of
                // Strategy should be used.
                Map<MetricType, MPVariable> qualityVariables = new HashMap<>();
                Function<MetricType, Function<UpdateNode, Tuple2<MPVariable, Double>>> nq2t = q -> r -> Tuple
                                .of(GraphLP.releaseVariable(solver, r), r
                                                .get(q).orElseThrow(
                                                                () -> new IllegalArgumentException("missing quality "
                                                                                + q + " on node " + r.id())));
                for (MetricType q : updatePreferences.qualityMetrics()) {
                        qualityVariables.put(q,
                                        solver.makeNumVar(0.0, Double.POSITIVE_INFINITY, "Quality[" + q + "]"));
                        List<Tuple2<MPVariable, Double>> qualities = updateGraph.releaseNodes().stream()
                                        .map(nq2t.apply(q)).toList();
                        OrHelpers.sum_ki_times_xi_eq_k_times_y(solver, "Quality[" + q + "] Constraint",
                                        qualities, 1.0, qualityVariables.get(q));
                }

                // Constraint for total quality
                // FIXME: check get is harmless here
                Function<MetricType, Tuple2<MPVariable, Double>> q2t = q -> Tuple.of(qualityVariables.get(q),
                                updatePreferences.coefficientFor(q).get());
                List<Tuple2<MPVariable, Double>> qualities = updatePreferences.qualityMetrics().stream().map(q2t)
                                .toList();
                OrHelpers.sum_ki_times_xi_eq_k_times_y(solver, "QUALITY Constraint", qualities, 1.0,
                                totalQuality);

                // Constraint for total cost (cost is only on change edges)
                // totalCost = sum_{d in changeEdges} (var_d * cost(d))
                // TODO: by now only one cost metric is used
                // TODO: is it ok to put 0 if cost is not found?
                Function<E, Tuple2<MPVariable, Double>> ce2t = ce -> Tuple.of(
                                GraphLP.changeVariable(solver, updateGraph, ce),
                                ce.get(costMetric).orElse(0.0));
                List<Tuple2<MPVariable, Double>> costs = updateGraph.changeEdges().stream().map(ce2t).toList();
                OrHelpers.sum_ki_times_xi_eq_k_times_y(solver, "COST Constraint", costs, 1.0, totalCost);

                // scaling factors for quality vs cost
                double costScaleFactor = updatePreferences.coefficientFor(costMetric).orElse(0.0);
                double qualityScaleFactor = 1 - costScaleFactor;

                // Objective function
                solver.objective().setCoefficient(totalQuality, qualityScaleFactor);
                solver.objective().setCoefficient(totalCost, costScaleFactor);
                solver.objective().setMinimization();

                return solver;
        }
}
