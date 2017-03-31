package IA;

import aima.search.framework.HeuristicFunction;

public class IAHeuristicFunction implements HeuristicFunction {

    public double getHeuristicValue(Object n){

        return ((IAState) n).heuristic2();
    }
}
