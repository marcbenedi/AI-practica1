package IA;

/**
 * Created by bejar on 17/01/17.
 */

import aima.search.framework.HeuristicFunction;

public class IAHeuristicFunction implements HeuristicFunction {

    public double getHeuristicValue(Object n){

        return ((IAState) n).heuristic2();
    }
}
