package client;

import updater.api.graph.structure.UpdateEdge;
import updater.api.graph.structure.UpdateGraph;
import updater.api.graph.structure.UpdateNode;
import updater.api.preferences.Preferences;
import updater.api.process.graphbased.UpdateSolver;
import static updater.impl.metrics.SimpleMetricType.*;
import updater.impl.mock.graph.GraphMock;
import updater.impl.mock.preferences.PreferencesMock;
import updater.impl.process.graphbased.lpga.LPGAUpdateSolver;
import util.helpers.system.LoggerHelpers;
import util.helpers.system.LoggerHelpers.Level;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import io.vavr.Tuple2;

/**
 * Basic client for LPGA project update in the Maven/Maven Central eco-system.
 * This is a mock client for testing purposes only. It may not run the whole
 * update process.
 */
public class ClientMock {

    public static void main(String[] args) {
        // default log level (DO NOT USE LOW FOR BIG GRAPHS)
        LoggerHelpers.instance().setLevel(Level.INFO); // LOW, INFO, ...
        // inputs
        final int EXAMPLE = -4;
        UpdateGraph<UpdateNode, UpdateEdge> g = switch (EXAMPLE) {
            case -1 -> GraphMock.generateExample001(10   , 10);
            case -2 -> GraphMock.generateExample002(10   , 10);
            case -3 -> GraphMock.generateExample003(10   , 10);
            case -4 -> GraphMock.generateExample004(1000   , 10);
            // Macbook Pro, M1 MAX, 64 Go
            //
            // 2024-03-26
            // ----------
            //
            // first set of experiments, with constraints in LP relative to change arcs missing (this is 2 constraints for each change arc, there are n*m*m changes arcs, eg 400,000 on a 1000x20 problem which means 800,000 constraints missing !)
            //
            // 1st experiment:
            // generateExample001
            // no multi-core specified, regular structure, zero quality, no dependencies
            //
            //   #A, #R/A, Q?, cores :  solving time : total time
            //   10,   10,  0,   -   :       25-30ms :        <1s
            //  100,   10,  0,   -   :       45-50ms :        <1s
            // 1000,   20,  0,   -   :     475-500ms :         9s
            // 1000,   50,  0,   -   :   1200-1300ms :        60s
            //
            // 2nd experiment:
            // generateExample001
            // multi-core specified, regular structure, zero quality, no dependencies
            //
            //   #A, #R/A, Q?, cores :  solving time : total time
            //   10,   10,  0,   8   :       25-30ms :        <1s   = / = (1)
            //  100,   10,  0,   8   :       45-50ms :        <1s   = / = (1)
            // 1000,   20,  0,   8   :     500-520ms :        10s   ~ / ~ (1)
            // 1000,   50,  0,   8   :   1200-1300ms :        60s   ~ / ~ (1)
            //
            // 3rd experiment:
            // generateExample002
            // no multi-core specified, regular structure, zero quality, regular dependencies
            //
            //   #A, #R/A, Q?, cores :  solving time : total time
            //   10,   10,  0,   -   :       45-50ms :        <1s    > / =  (1,2)
            //  100,   10,  0,   -   :     250-260ms :        <1s    > / =  (1,2)
            // 1000,   20,  0,   -   :   8100-8400ms :   3m-3m30s   >> / >> (1,2)
            // 1000,   50,  0,   -   :  -untested-   : -untested- 
            //
            // 4th experiment:
            // generateExample002
            // multi-core specified, regular structure, zero quality, regular dependencies
            //
            //   #A, #R/A, Q?, cores :  solving time : total time
            //   10,   10,  0,   8   :       40-50ms :        <1s    ~ / ~  (3)
            //  100,   10,  0,   8   :     250-260ms :        <1s    ~ / ~  (3)
            // 1000,   20,  0,   8   :   8100-8400ms :   3m-3m30s    ~ / ~  (3)
            // 1000,   50,  0,   8   :  -untested-   : -untested- 
            //
            // 5th experiment:
            // generateExample002
            // multi-core specified, regular structure, zero quality, regular dependencies
            //
            //   #A, #R/A, Q?, cores :  solving time : total time
            // 1000,   10,  0,   8   :   2765-2800ms :     25-26s
            // 2000,   10,  0,   8   : 12600-14000ms :1m50s-2m10s
            // reference (from 4)
            // 1000,   20,  0,   8   :   8100-8400ms :   3m-3m30s
            //
            // COMMENTS: (WRT MY EXAMPLE GENERATOR and the structure of problems is creates)
            // - Adding cores does not seem to change things.
            //   indeed we have a message 
            //    No match for threads - ? for list of commands
            //    No match for 8 - ? for list of commands
            //   meaning possibly that the chosen solver (CBC) does not support threads ?
            // - Number of variables, O(n*m) by now, seems ok up to 1000x20
            // - Number of constraints, 
            // - But change edge constraints will add n*m*m constraints
            // - possibly we have to limit the number of versions (m) to eg the last 10?
            //   (taking into account that we have to keep the "used" versions anyway)
            // - other solver ? (GLOP:no) how to deal with cores ?
            // - interesting result of 1000x20 vs 2000x10. solving vs total time.
            //   which time to take into account? we could possible "save" things and use
            //   only the solving time if we change the weights for example (supposing the
            //   biggest time spent is building graphs and generating the problem)
            // - it should be also noted that, by now, time does not include post-treatment
            //   of the solution (computing new graph + new project)
            //
            // next experiments : 
            // - move n and m to "realistic" size, build "realistic" problem structures
            //   (ask Damien to make experiments to measure things on REAL example graphs)
            // - add Q=1 everywhere (requires more constraints)
            // - add the constraints on change edges wrt source/target presence in solution
            //
            // 2024-03-26
            // ----------
            //
            // 6th experiment:
            // UPDATED LPGA Solver : added constraints for change links
            // generateExample002
            // multi-core specified, regular structure, zero quality, regular dependencies
            //
            //   #A, #R/A, Q?, cores :  solving time : total time
            //   10,   10,  0,   8   :   2350-2400ms :         3s    >> / >>
            //  100,   10,  0,   8   : 10200-10300ms :        11s    >> / >>
            // 1000,   10,  0,   8   : 1,390s-1,450s :     23-24m    >> / >>
            // 2000,   10,  0,   8   :    -stopped-  : -stopped-
            // reference (from 4)
            //   10,   10,  0,   8   :       40-50ms :        <1s
            //  100,   10,  0,   8   :     250-260ms :        <1s
            // 1000,   20,  0,   8   :   8100-8400ms :   3m-3m30s
            // reference (from 5)
            // 1000,   10,  0,   8   :   2765-2800ms :     25-26s
            // 2000,   10,  0,   8   : 12600-14000ms :1m50s-2m10s
            //
            // COMMENTS:
            // - change arcs seem very costly, is there a way to have less of them (or do not use them)? In reality, we do not have a lot of change arcs but possibly for the root. -> develop a new example generator that takes this into account given some reality measures.
            // - this raises the question of synthetic examples vs reality. We need a set of real examples soon.
            //
            // 7th experiment:
            // UPDATED Graph Generation: only change edges from root.
            // generateExample003
            // multi-core specified, regular structure, zero quality, regular dependencies
            //
            //   #A, #R/A, Q?, cores :  solving time : total time
            //   10,   10,  0,   8   :          40ms :        <1s
            //  100,   10,  0,   8   :     190-195ms :        <1s
            // 1000,   10,  0,   8   :   2280-2300ms :   6,5-7,5s
            // 2000,   10,  0,   8   :   5835-5855ms :     26-27s
            // reference (from 6)
            //   10,   10,  0,   8   :   2350-2400ms :         3s
            //  100,   10,  0,   8   : 10200-10300ms :        11s
            // 1000,   10,  0,   8   : 1,390s-1,450s :     23-24m
            // 2000,   10,  0,   8   :    -stopped-  : -stopped-
            // reference (from 4)
            //   10,   10,  0,   8   :       40-50ms :        <1s
            //  100,   10,  0,   8   :     250-260ms :        <1s
            // 1000,   20,  0,   8   :   8100-8400ms :   3m-3m30s
            // reference (from 5)
            // 1000,   10,  0,   8   :   2765-2800ms :     25-26s
            // 2000,   10,  0,   8   : 12600-14000ms :1m50s-2m10s
            //
            // COMMENTS:
            // - change arc are clearly a big issue. One should have the less possible.
            // - this does not mean we do not have choices to make (we still have m versions per artifact), just that we won't be able to take into account a cost everyhere
            //
            // 8th experiment:
            // UPDATED Graph Generation: "realistic" freshness values
            // generateExample004
            // multi-core specified, regular structure, freshness+popularity quality, regular dependencies
            //
            //   #A, #R/A, Q?, cores :  solving time : total time
            //   10,   10, PF,   8   :          40ms :        <1s
            //  100,   10, PF,   8   :     375-390ms :        <1s    > / =
            // 1000,   10, PF,   8   :        23-24s :     28-30s   >> / >>
            // 2000,   10, PF,   8   :   ??????????s :     ?????s
            // reference (from 7)
            //   10,   10,  0,   8   :          40ms :        <1s
            //  100,   10,  0,   8   :     190-195ms :        <1s
            // 1000,   10,  0,   8   :   2280-2300ms :   6,5-7,5s
            // 2000,   10,  0,   8   :   5835-5855ms :     26-27s
            //
            // COMMENTS:
            // - the demo popularity measure used in synthetic graph generation is bad (not really discriminating version 1 to 6), anyway, it is not the objective here
            //
            case 1 -> GraphMock.example001();
            case 2 -> GraphMock.example002();
            default -> throw new IllegalArgumentException("Invalid example");
        };
        List<Tuple2<String, Integer>> constrainedValues = List.of(
        // Tuple.of("e:e:1", 0),
        // Tuple.of("e:e:2", 0) //,
        // Tuple.of("f", 1)
        );
        Preferences prefs = new PreferencesMock(Map.of(
                CVE, 0.5,
                FRESHNESS, 0.3,
                POPULARITY, 0.2,
                COST, 0.6));
        // create solver and resolve update
        UpdateSolver solver = new LPGAUpdateSolver(constrainedValues);
        Optional<UpdateGraph<UpdateNode, UpdateEdge>> gprime = solver.resolve(g, prefs);
    }
}
