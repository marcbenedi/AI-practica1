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
        myState.printState();

        System.out.println("Calling the heuristic function - FOR TESTING PURPOSES");
        Double t = myState.heuristic1();
        System.out.println(t);

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
        System.out.println("We found the best local solution");
        printActions(agent.getActions());
        printInstrumentation(agent.getInstrumentation());

        // You can access also to the goal state using the
        // method getGoalState of class Search
        IAState fin = (IAState) alg.getGoalState();
        System.out.println(fin.heuristic1());
        System.out.println(fin.calculPrint());
    }

    private static void printInstrumentation(Properties properties) {
        Iterator keys = properties.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String property = properties.getProperty(key);
            System.out.println(key + " : " + property);
        }

    }



    private static void printActions(List actions) {
        for (int i = 0; i < actions.size(); i++) {
            String action = (String) actions.get(i);
            System.out.println(action);
        }
    }

}
