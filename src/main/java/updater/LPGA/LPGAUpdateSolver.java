package updater.lpga;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import addedvalue.AddedValueEnum;
import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import graph.structures.UpdateGraph;
import io.vavr.Tuple2;
import updater.UpdateSolver;
import updater.preferences.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
            UpdatePreferences updatePreferences) {
        Loader.loadNativeLibraries();
        MPSolver problem = createProblem(updateGraph, updatePreferences);
        integrateConstrainedValues(updateGraph, problem);
        return solveProblem(problem, updateGraph, updatePreferences)
                .flatMap(solution -> solutionToGraph(solution, updateGraph, updatePreferences));
    }

    private Optional<UpdateGraph<UpdateNode, UpdateEdge>> solutionToGraph(MPSolver solution,
            UpdateGraph<UpdateNode, UpdateEdge> updateGraph, UpdatePreferences updatePreferences) {
        OrLP.printSolution(solution);
        // TODO: JOYCE en second
        return Optional.empty();
    }

    private Optional<MPSolver> solveProblem(MPSolver problem, UpdateGraph<UpdateNode, UpdateEdge> updateGraph,
            UpdatePreferences updatePreferences) {
        MPSolver.ResultStatus result = problem.solve();
        if (result == MPSolver.ResultStatus.OPTIMAL || result == MPSolver.ResultStatus.FEASIBLE) {
            System.out.println("Problem solved");
            OrLP.printProblem(problem);
            return Optional.of(problem);
        } else {
            System.out.println("Problem not solved");
            OrLP.printProblem(problem);
            return Optional.empty();
        }
    }

    private <N extends UpdateNode, E extends UpdateEdge> void integrateConstrainedValues(UpdateGraph<N, E> updateGraph, MPSolver problem) {
        for (Tuple2<String, Integer> t : constrainedValues) {
            String e = t._1();
            Integer v = t._2();
            N node = updateGraph.nodes(n -> n.id().equals(e)).stream().findFirst().orElse(null); // node should exists
            MPVariable excludedVariable = GraphLP.releaseVariable(problem, node);
            OrLP.makeEqualityConstraint(problem, "constraint on " + e, excludedVariable, v);
        }
    }

    private <N extends UpdateNode, E extends UpdateEdge> MPSolver createProblem(UpdateGraph<N, E> updateGraph,
            UpdatePreferences updatePreferences) {
        MPSolver solver = MPSolver.createSolver("GLOP");
        // FIXME: moche et pas efficace !!
        Set<N> artifactNodes = updateGraph.artifactNodes();
        Set<N> releaseNodes = updateGraph.releaseNodes();
        Set<E> versionEdges = updateGraph.versionEdges();
        Set<E> dependencyEdges = updateGraph.dependencyEdges();
        // Set<E> changeEdges = updateGraph.changeEdges();

        // create a variable for each node in releaseNodes
        releaseNodes.forEach(n -> solver.makeBoolVar(GraphLP.releaseVariableName(n)));

        // create a variable for each node in artifactNodes
        artifactNodes.forEach(n -> solver.makeBoolVar(GraphLP.artifactVariableName(n)));

        // create a constraint stating that the variable for the root node equals 1
        // (ROOT)
        updateGraph.rootNode().ifPresent(n -> OrLP.makeEqualityConstraint(solver,
                "ROOT is " + n.id(),
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
            OrLP.makeEqualityWithSumConstraint(solver,
                    "VERSIONS " + GraphLP.artifactVariableName(a) + "->"
                            + releases.stream().map(r -> r.name()).collect(Collectors.joining(", ")),
                    releases,
                    1, vA);
            List<MPVariable> dependants = dependencyEdges.stream()
                    .filter(e -> updateGraph.target(e) == a)
                    .map(updateGraph::source)
                    .map(r -> GraphLP.releaseVariable(solver, r))
                    .toList();
            OrLP.makeSupEqualWithSumConstraint(solver, "DEP1 " + aName, dependants, vA);
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
            OrLP.makeSupEqualConstraint(solver, "DEP2 " + sName + "->" + tName, tVar, sVar);
        }

        // optimization part
        // FIXME: make it work with UpdatePreferences (choice of metrics + weights) not ad-hoc
        // FIXME: CORRECT CODE

        /*
        // values on nodes (normalized) : CVE, FRESHNESS, POPULARITY
        double[] cveMetrics;
        double[] freshnessMetrics;
        double[] popularityMetrics;

        // values on change edges : COST (MARACAS)
        double[][] costMetrics;

        // weights
        double[] weights;


        // CVE aggregative variable    
        foreach (realsenode -> ) {
                    solver.makeConstraint(0, Double.POSITIVE_INFINITY, "CVEConstraint[" + i + "]")
                            .setCoefficient(Varnodes[i], cveMetrics[i]);
                }
        foreach (possibleupdateedge -> ) {
                    solver.makeConstraint(0, Double.POSITIVE_INFINITY, "CVEConstraint[" + i + "]")
                            .setCoefficient(Varnodes[i]*Varnodes[j], costMetrics[i][0]);
                }
        
                           .setCoefficient(totalQualityCVE, -1);

        // Freshness aggregative variable
foreach (releasenode -> ){
            solver.makeConstraint(0, Double.POSITIVE_INFINITY, "FreshConstraint")
                    .setCoefficient(Varnodes[i], freshnessMetrics[i]);
             }.setCoefficient(totalQualityFresh, -1);

        // Popularity aggregative variable
        // Definir la fonction aggregative pour la popularité
foreach (releasenode -> ){
            solver.makeConstraint(0, Double.POSITIVE_INFINITY, "QualityConstraint[" + i + "]")
                    .setCoefficient(nodes[i], popularityMetrics[i]);
        }
                   .setCoefficient(totalQualityPop, -graph.size());

        // Quality aggregative variable
for (int i = 0; i < 3; i++) {
            solver.makeConstraint(0, Double.POSITIVE_INFINITY, "TotalqualityConstraint")
                    .setCoefficient(totalQualityCVE, weights[1]);
                    .setCoefficient(totalQualityFresh, weights[2]);
                    .setCoefficient(totalQualityPop, weights[3]);  
             }.setCoefficient(totalQuality, -1);
        
        // Cost aggregative variable
foreach (possibleupdateedge -> ) {
            solver.makeConstraint(0, Double.POSITIVE_INFINITY, "totalcostConstraint")
                    .setCoefficient(varnodes[i]*varnode[j], costMetrics[i][1]);
        }.setCoefficient(totalCost, -1);

        */

        // Metric variables
        Map<AddedValueEnum, MPVariable> metricVariables = new HashMap<>();
        for (AddedValueEnum addedValue : updatePreferences.metrics()) { // TODO: check somewhere that user preferences only use known metrics and have at least 1 quality metric + cost
            metricVariables.put(addedValue, solver.makeNumVar(0.0, Double.POSITIVE_INFINITY, "Total[" + addedValue + "]"));
        }

        // Aggregative variables
        MPVariable totalQuality = solver.makeNumVar(0.0, Double.POSITIVE_INFINITY, "TotalQuality");
        MPVariable totalCost = solver.makeNumVar(0.0, Double.POSITIVE_INFINITY, "TotalCost");

        // scaling factors for quality vs cost
        // TODO: use and compute from user preferences instead
        double qualityScaleFactor = 0.5;
        double costScaleFactor = 0.5;

        // Objective function
        solver.objective().setCoefficient(totalQuality, qualityScaleFactor);
        solver.objective().setCoefficient(totalCost, costScaleFactor);
        solver.objective().setMinimization();

        return solver;
    }
}
