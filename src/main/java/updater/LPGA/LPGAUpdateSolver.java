package updater.LPGA;

import updater.updatePreferences.*;
import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import graph.entities.edges.UpdateEdge;
import graph.entities.nodes.UpdateNode;
import graph.structures.CustomGraph;
import graph.structures.UpdateGraph;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import updater.UpdateSolver;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LPGAUpdateSolver implements UpdateSolver {
    @Override
    public <N extends UpdateNode, E extends UpdateEdge>Optional<CustomGraph> resolve(UpdateGraph<N, E> updateGraph, UpdatePreferences updatePreferences) {
        //TODO
        Loader.loadNativeLibraries();
        MPSolver problem = createProblem(updateGraph, updatePreferences);
        return solveProblem(problem, updateGraph, updatePreferences).flatMap(solution -> solutionToGraph(solution, updateGraph, updatePreferences));
    }

    private Optional<CustomGraph> solutionToGraph(MPSolver solution, CustomGraph updateGraph, UpdatePreferences updatePreferences) {
        OrLP.printSolution(solution);
        // TODO: JOYCE en second
        return Optional.empty();
    }

    private Optional<MPSolver> solveProblem(MPSolver problem, CustomGraph updateGraph, UpdatePreferences updatePreferences) {
        MPSolver.ResultStatus result = problem.solve();
        if (result == MPSolver.ResultStatus.OPTIMAL || result == MPSolver.ResultStatus.FEASIBLE) {
            return Optional.of(problem);
        } else {
            System.out.println("Problem not solved");
            OrLP.printProblem(problem);
            return Optional.empty();
        }
    }

    private <N extends UpdateNode,E extends UpdateEdge> MPSolver createProblem(UpdateGraph<N, E> updateGraph, UpdatePreferences updatePreferences) {
        MPSolver solver = MPSolver.createSolver("GLOP");
        // FIXME: moche et pas efficace !!
        Set<N> artifactNodes = updateGraph.releaseNodes();
        Set<N> libraryNodes = updateGraph.libraryNodes();
        Set<E> versionEdges = updateGraph.versionEdges();
        Set<E> dependencyEdges = updateGraph.dependencyEdges();
        Set<E> possibilityEdges = updateGraph.possibleEdges();

        // create a variable for each node in artifactNodes
        artifactNodes.forEach(n -> solver.makeBoolVar(GraphLP.artifactVariableName(n)));

        // create a variable for each node in libraryNodes
        libraryNodes.forEach(n -> solver.makeBoolVar(GraphLP.libraryVariableName(n)));

        // create a variable for each node in libraryNodes
        // FIXME: this is costly ...
        // libraryNodes.forEach(n -> {
        //     long maxPossibleDependents = dependencyEdges.stream().filter(e -> graph.target(e).equals(n)).count();
        //     solver.makeIntVar(0, maxPossibleDependents, GraphLP.libraryVariableName(n));
        // });

        // create a variable for each edge in dependencyEdges
        // dependencyEdges.forEach(
        //         e -> solver.makeBoolVar(GraphLP.dependencyVariableName(graph, e)));

        // create a variable for each edge in possibilityEdges
        // possibilityEdges.forEach(
        //         e -> solver.makeBoolVar(GraphLP.possibilityVariableName(graph, e)));

        // testing constraints on artifact nodes
        // TODO: for tests only, to be removed later
        List<Tuple2<String, Integer>> testValues = List.of(
                Tuple.of("j",0),
                Tuple.of("d",0),
                Tuple.of("f", 1));
        for (Tuple2<String, Integer> t : testValues) {
            String e = t._1();
            Integer v = t._2();
            N excludedNode = updateGraph.nodes(n -> n.getId().equals(e)).stream().findFirst().orElse(null); // node should exists !
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
                    "VERSION " + lib.getId(),
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

        // TODO: JOYCE partie qualitative
        return solver;
    }
}
