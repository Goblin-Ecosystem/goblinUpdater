package updater.impl.process.graphbased.lpga;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import updater.api.graph.UpdateGraph;
import updater.api.metrics.MetricType;
import updater.api.preferences.Preferences;
import updater.api.process.graphbased.UpdateSolver;
import updater.impl.graph.edges.UpdateEdge;
import updater.impl.graph.nodes.UpdateNode;
import util.helpers.or.OrHelpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class LPGAUpdateSolver implements UpdateSolver {

        private List<Tuple2<String, Integer>> constrainedValues;

        public LPGAUpdateSolver() {
        }

        public LPGAUpdateSolver(List<Tuple2<String, Integer>> constrainedValues) {
                this();
                this.setConstrainedValues(constrainedValues);
        }

        private void setConstrainedValues(List<Tuple2<String, Integer>> constrainedValues) {
                this.constrainedValues = constrainedValues;
        }

        @Override
        public Optional<UpdateGraph<UpdateNode, UpdateEdge>> resolve(UpdateGraph<UpdateNode, UpdateEdge> updateGraph,
                        Preferences updatePreferences) {
                Loader.loadNativeLibraries();
                MPSolver problem = createProblem(updateGraph, updatePreferences);
                integrateConstrainedValues(updateGraph, problem);
                return solveProblem(problem, updateGraph, updatePreferences)
                                .flatMap(solution -> solutionToGraph(solution, updateGraph, updatePreferences));
        }

        private Optional<UpdateGraph<UpdateNode, UpdateEdge>> solutionToGraph(MPSolver solution,
                        UpdateGraph<UpdateNode, UpdateEdge> updateGraph, Preferences updatePreferences) {
                OrHelpers.printSolution(solution);
                // TODO: JOYCE en second
                return Optional.empty();
        }

        private Optional<MPSolver> solveProblem(MPSolver problem, UpdateGraph<UpdateNode, UpdateEdge> updateGraph,
                        Preferences updatePreferences) {
                MPSolver.ResultStatus result = problem.solve();
                if (result == MPSolver.ResultStatus.OPTIMAL || result == MPSolver.ResultStatus.FEASIBLE) {
                        System.out.println("Problem solved");
                        OrHelpers.printProblem(problem);
                        return Optional.of(problem);
                } else {
                        System.out.println("Problem not solved");
                        OrHelpers.printProblem(problem);
                        return Optional.empty();
                }
        }

        private <N extends UpdateNode, E extends UpdateEdge> void integrateConstrainedValues(
                        UpdateGraph<N, E> updateGraph,
                        MPSolver problem) {
                for (Tuple2<String, Integer> t : constrainedValues) {
                        String e = t._1();
                        Integer v = t._2();
                        N node = updateGraph.nodes(n -> n.id().equals(e)).stream().findFirst().orElse(null); // node
                                                                                                             // should
                                                                                                             // exists
                        MPVariable excludedVariable = GraphLP.releaseVariable(problem, node);
                        OrHelpers.makeEqualityConstraint(problem, "constraint on " + e, excludedVariable, v);
                }
        }

        private <N extends UpdateNode, E extends UpdateEdge> MPSolver createProblem(UpdateGraph<N, E> updateGraph,
                        Preferences updatePreferences) {
                // FIXME: select the best solver. CBC, CLP, GLPK, CP-SAT, ... Taking care with
                // the correct treatment for "binary" variables (that should be 0 or 1 not 0.5
                // for example)
                // GLOP is not working correctly with binary variables. Also all linear solvers?
                MPSolver solver = MPSolver.createSolver("CBC");
                solver.setNumThreads(8);
                // FIXME: moche et pas efficace !!
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
                updateGraph.rootNode().ifPresent(n -> OrHelpers.makeEqualityConstraint(solver,
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
                        OrHelpers.makeEqualityWithSumConstraint(solver,
                                        "VERSIONS of " + GraphLP.artifactVariableName(a),
                                        releases,
                                        1, vA);
                        List<MPVariable> dependants = dependencyEdges.stream()
                                        .filter(e -> updateGraph.target(e) == a)
                                        .map(updateGraph::source)
                                        .map(r -> GraphLP.releaseVariable(solver, r))
                                        .toList();
                        OrHelpers.makeSupEqualWithSumConstraint(solver, "DEP1 " + aName, dependants, vA);
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
                        OrHelpers.makeSupEqualConstraint(solver, "DEP2 " + sName + " " + tName, tVar, sVar);
                }

                // FIXME: create constraints on change edges
                // create constraints for possibilities

                // OPTION 1
                // --------
                //
                // 1.1.) there is at most one possible arc for each dependency
                // for all d=(s,t) in dependencyEdges, sum_{p in possibles(s,t)} (var_p) = var_s
                // NOTE: this does not mean 1.2,
                // e.g., if var_s=var_(s,v1)=var_v2=1 and var_(s,v2)=var_v1=0.
                // (hence 1.2 is required)
                //
                // 1.2.) if there is a possible arc then its source and target are there
                // for all p=(s,u) in possibilityEdges, var_s >= var_p and var_u >= var_p
                // i.e., var_p => var_s and var_p => var_u
                // NOTE: this does not mean that (var_s and var_u) => var_p, hence 1.1.

                // OPTION 2
                // --------
                //
                // for all d=(s,u) in possibilityEdges, (var_s and var_u) => var_p
                // problem: how to express this in PL?

                // TODO: rather fail using Optional or Either, which means problem generation
                // can fail too (not only solving it)
                if (!updatePreferences.isValid()) {
                        throw new IllegalArgumentException("Update preferences are not valid");
                }
                MetricType costAddedValue = updatePreferences.costMetrics().stream().findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("No cost added value defined"));

                // Aggregative variables
                MPVariable totalQuality = solver.makeNumVar(0.0, Double.POSITIVE_INFINITY, "QUALITY");
                MPVariable totalCost = solver.makeNumVar(0.0, Double.POSITIVE_INFINITY, "COST");

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
                        OrHelpers.makeEqualityWithWeightedSumConstraint(solver, "Quality[" + q + "] Constraint",
                                        qualities, 1.0, qualityVariables.get(q));
                }

                // Constraint for total quality
                Function<MetricType, Tuple2<MPVariable, Double>> q2t = q -> Tuple.of(qualityVariables.get(q),
                                updatePreferences.coefficientFor(q));
                List<Tuple2<MPVariable, Double>> qualities = updatePreferences.qualityMetrics().stream().map(q2t)
                                .toList();
                OrHelpers.makeEqualityWithWeightedSumConstraint(solver, "QUALITY Constraint", qualities, 1.0,
                                totalQuality);

                // Constraint for total cost (cost is only on change edges)
                // totalCost = sum_{d in changeEdges} (var_d * cost(d))
                // TODO: by now only one cost metric is used
                Function<E, Tuple2<MPVariable, Double>> ce2t = ce -> Tuple.of(
                                GraphLP.changeVariable(solver, updateGraph, ce),
                                0.0);
                List<Tuple2<MPVariable, Double>> costs = updateGraph.changeEdges().stream().map(ce2t).toList();
                OrHelpers.makeEqualityWithWeightedSumConstraint(solver, "COST Constraint", costs, 1.0, totalCost);

                // scaling factors for quality vs cost
                double costScaleFactor = updatePreferences.coefficientFor(costAddedValue);
                double qualityScaleFactor = 1 - costScaleFactor;

                // Objective function
                solver.objective().setCoefficient(totalQuality, qualityScaleFactor);
                solver.objective().setCoefficient(totalCost, costScaleFactor);
                solver.objective().setMinimization();

                return solver;
        }
}
