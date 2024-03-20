package updater.lpga;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import graph.structures.UpdateGraph;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import updater.UpdateSolver;
import updater.preferences.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LPGAUpdateSolver implements UpdateSolver {
    @Override
    public Optional<UpdateGraph<UpdateNode, UpdateEdge>> resolve(UpdateGraph<UpdateNode, UpdateEdge> updateGraph,
            UpdatePreferences updatePreferences) {
        // TODO
        Loader.loadNativeLibraries();
        MPSolver problem = createProblem(updateGraph, updatePreferences);
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
            return Optional.of(problem);
        } else {
            System.out.println("Problem not solved");
            OrLP.printProblem(problem);
            return Optional.empty();
        }
    }

    private <N extends UpdateNode, E extends UpdateEdge> MPSolver createProblem(UpdateGraph<N, E> updateGraph,
            UpdatePreferences updatePreferences) {
        MPSolver solver = MPSolver.createSolver("GLOP");
        // FIXME: moche et pas efficace !!
        Set<N> artifactNodes = updateGraph.releaseNodes();
        Set<N> libraryNodes = updateGraph.artifactNodes();
        Set<E> versionEdges = updateGraph.versionEdges();
        Set<E> dependencyEdges = updateGraph.dependencyEdges();
        Set<E> changeEdges = updateGraph.changeEdges();

        // create a variable for each node in artifactNodes
        artifactNodes.forEach(n -> solver.makeBoolVar(GraphLP.artifactVariableName(n)));

        // create a variable for each node in libraryNodes
        libraryNodes.forEach(n -> solver.makeBoolVar(GraphLP.libraryVariableName(n)));

        // testing constraints on artifact nodes
        // TODO: for tests only, to be removed later
        List<Tuple2<String, Integer>> testValues = List.of(
                Tuple.of("j", 0),
                Tuple.of("d", 0),
                Tuple.of("f", 1));
        for (Tuple2<String, Integer> t : testValues) {
            String e = t._1();
            Integer v = t._2();
            N excludedNode = updateGraph.nodes(n -> n.id().equals(e)).stream().findFirst().orElse(null); // node should
                                                                                                         // exists !
            MPVariable excludedVariable = GraphLP.artifactVariable(solver, excludedNode);
            OrLP.makeEqualityConstraint(solver, e + " not in solution", excludedVariable, v);
        }

        // create a constraint stating that the variable for the root node equals 1
        // (ROOT)
        updateGraph.rootNode().ifPresent(n -> OrLP.makeEqualityConstraint(solver,
                "ROOT",
                GraphLP.artifactVariable(solver, n),
                1));

        // create constraints for versions (VER)
        // for all l in libraryNodes, sum_{a in versions(l)} (var_a) = var_l
        // and
        // create constraints for dependencies (DEP 1)
        // for all l in libraryNodes, sum_{a in dependants(l)} (var_a) >= var_l
        for (N lib : libraryNodes) {
            String libName = GraphLP.libraryVariableName(lib);
            MPVariable vLib = GraphLP.libraryVariable(solver, lib);
            List<MPVariable> artifacts = versionEdges.stream()
                    .filter(e -> updateGraph.source(e) == lib)
                    .map(updateGraph::target)
                    .map(a -> GraphLP.artifactVariable(solver, a))
                    .toList();
            OrLP.makeEqualityWithSumConstraint(solver,
                    "VERSION " + lib.id(),
                    artifacts,
                    1, vLib);
            List<MPVariable> dependants = dependencyEdges.stream()
                    .filter(e -> updateGraph.target(e) == lib)
                    .map(updateGraph::source)
                    .map(a -> GraphLP.artifactVariable(solver, a))
                    .toList();
            OrLP.makeSupEqualWithSumConstraint(solver, "DEP1 " + libName, dependants, vLib);
        }

        // create constraints for dependencies (DEP2)
        // for all d=(s,t) in dependencyEdges, var_t >= var_s
        for (E d : dependencyEdges) {
            N source = updateGraph.source(d);
            N target = updateGraph.target(d);
            String sName = GraphLP.artifactVariableName(source);
            String tName = GraphLP.artifactVariableName(target);
            MPVariable sVar = GraphLP.artifactVariable(solver, source);
            MPVariable tVar = GraphLP.libraryVariable(solver, target);
            OrLP.makeSupEqualConstraint(solver, "DEP2 " + sName + "->" + tName, tVar, sVar);
        }

        // TODO: JOYCE partie qualitative
        return solver;
    }
}
