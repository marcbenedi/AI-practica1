import IA.IAHeuristicFunction;
import IA.IAState;
import IA.IASuccesorFunction;
import IA.ProbIA5GoalTest;
import aima.search.framework.Problem;
import aima.search.framework.Search;
import aima.search.framework.SearchAgent;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws Exception {

        int num_c = Integer.parseInt(args[0]);
        int num_s = Integer.parseInt(args[1]);
        int seed_c = Integer.parseInt(args[2]);
        int seed_s = Integer.parseInt(args[3]);
        int op_id = Integer.parseInt(args[4]);
        int gen_id = Integer.parseInt(args[5]);
        //System.out.println("These are the arguments of the program: " + num_c + " " + num_s + " " +
        //        seed_c + " " + seed_s + " " + op_id + " " + gen_id );

        long millis = System.currentTimeMillis();

        IAState myState = new IAState(num_c,num_s,seed_c,seed_s,gen_id);

        // Create the Problem object
        Problem p = new  Problem(
                myState,
                new IASuccesorFunction(op_id),
                new ProbIA5GoalTest(),
                new IAHeuristicFunction());

        // Instantiate the search algorithm
        Search alg = new HillClimbingSearch();

        // k = 10, 5, 10,20
        // lamb = 0.01, 0.1, 0.005, 0.01
        // steps = 2244, 830, 4000, 11460
        // stiter =
        //Search alg2 = new SimulatedAnnealingSearch();

        // Instantiate the SearchAgent object
        SearchAgent agent = new SearchAgent(p, alg);

        // You can access also to the goal state using the
        // method getGoalState of class Search
        IAState fin = (IAState) alg.getGoalState();
        //System.out.println("The heuristic (2) value for the solution state is = " + fin.heuristic2());
        fin.printStateCSV();
        long m2 = System.currentTimeMillis();

        System.out.println((m2-millis));

        printInstrumentation(agent.getInstrumentation());
    }

    private static void printInstrumentation(Properties properties) {
        //System.out.println("Instrumentation:");
        Iterator keys = properties.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String property = properties.getProperty(key);
            System.out.println(/*key + " : " +*/ property);
        }
    }

    private static void printActions(List actions) {
        System.out.println("Actions done");
        for (int i = 0; i < actions.size(); i++) {
            String action = (String) actions.get(i);
            System.out.println(action);
        }
    }

}