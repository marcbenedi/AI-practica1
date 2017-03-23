import IA.IAHeuristicFunction;
import IA.IAState;
import IA.IASuccesorFunction;
import IA.ProbIA5GoalTest;
import aima.search.framework.Problem;
import aima.search.framework.Search;
import aima.search.framework.SearchAgent;
import aima.search.informed.HillClimbingSearch;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("Createing IAState");

        IAState myState = new IAState();
        //myState.printState();

        System.out.println("Calling the heuristic function - FOR TESTING PURPOSES");
        System.out.println("The initial state heuristic value is = "+myState.heuristic1());

        // Create the Problem object
        Problem p = new  Problem(
                myState,
                new IASuccesorFunction(),
                new ProbIA5GoalTest(),
                new IAHeuristicFunction());

        // Instantiate the search algorithm
        Search alg = new HillClimbingSearch();
        // Instantiate the SearchAgent object
        SearchAgent agent = new SearchAgent(p, alg);

        // We print the results of the search
        System.out.println("We have found the best local solution");
        // You can access also to the goal state using the
        // method getGoalState of class Search
        IAState fin = (IAState) alg.getGoalState();
        System.out.println("The heuristic value for the solution state is = " + fin.heuristic1());

        //printActions(agent.getActions());
        printInstrumentation(agent.getInstrumentation());

        System.out.println(fin.printTotalCostCalculation());
    }

    private static void printInstrumentation(Properties properties) {
        System.out.println("Instrumentation:");
        Iterator keys = properties.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String property = properties.getProperty(key);
            System.out.println(key + " : " + property);
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
