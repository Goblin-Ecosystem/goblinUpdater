package code.update.impl.or.solver;

import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import com.google.ortools.linearsolver.MPSolver.ResultStatus;

import code.graphs.api.Graph;
import code.metrics.api.MetricContainer;
import code.update.api.LPUpdateSolver;
import code.update.api.UpdateEdge;
import code.update.api.UpdateNode;
import code.update.impl.or.helpers.GraphLP;
import code.update.impl.or.helpers.OrLP;
import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.util.Optional;

import java.util.List;

public class OrLPUpdateSolver<N extends MetricContainer & UpdateNode, E extends MetricContainer & UpdateEdge>
        implements LPUpdateSolver<N, E, MPSolver, MPSolver> {

    @Override
    public MPSolver createProblem(Graph<N, E> graph, MetricContainer weights) {
        MPSolver solver = MPSolver.createSolver("GLOP");
        // FIXME: moche et pas efficace !!
        List<N> artifactNodes = graph.nodes(N::isArtifact);
        List<N> libraryNodes = graph.nodes(N::isLibrary);
        List<E> versionEdges = graph.edges(E::isVersion);
        List<E> dependencyEdges = graph.edges(E::isDependency);
        List<E> possibilityEdges = graph.edges(E::isPossible);

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
            N excludedNode = graph.nodes(n -> n.name().equals(e)).getFirst(); // node should exists !
            MPVariable excludedVariable = GraphLP.artifactVariable(solver, excludedNode);
            OrLP.makeEqualityConstraint(solver, e + " not in solution", excludedVariable, v);
        }

        // create a constraint stating that the variable for the root node equals 1
        // (ROOT)
        graph.rootNode().ifPresent(n -> OrLP.makeEqualityConstraint(solver,
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
                    .filter(e -> graph.source(e) == lib)
                    .map(graph::target)
                    .map(a -> GraphLP.artifactVariable(solver, a))
                    .toList();
            OrLP.makeEqualityWithSumConstraint(solver,
                    "VERSION " + lib.name(),
                    artifacts,
                    1, vLib);
            List<MPVariable> dependants = dependencyEdges.stream()
                   .filter(e -> graph.target(e) == lib)
                   .map(graph::source)
                   .map(a -> GraphLP.artifactVariable(solver, a))
                   .toList();
            OrLP.makeSupEqualWithSumConstraint(solver, "DEP1 " + libName, dependants, vLib);
        }

        // create constraints for dependencies (DEP2)
        // for all d=(s,t) in dependencyEdges, var_t >= var_s
        for (E d : dependencyEdges) {
            N source = graph.source(d);
            N target = graph.target(d);
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

    @Override
    public Optional<MPSolver> solveProblem(MPSolver problem, Graph<N, E> graph,
            MetricContainer weights) {
        ResultStatus result = problem.solve();
        if (result == ResultStatus.OPTIMAL || result == ResultStatus.FEASIBLE) {
            return Optional.of(problem);
        } else {
            System.out.println("Problem not solved");
            OrLP.printProblem(problem);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Graph<N, E>> solutionToGraph(MPSolver solution,
            Graph<N, E> graph) {
        // TODO: JOYCE en second
        OrLP.printSolution(solution);
        return Optional.empty();
    }

}
