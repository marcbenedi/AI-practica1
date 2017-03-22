package IA;

import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by bejar on 17/01/17.
 */
public class IASuccesorFunction implements SuccessorFunction{

    public List getSuccessors(Object state){
        IAState networkConf = (IAState) state;

        // Some code here
        // (flip all the consecutive pairs of coins and generate new states
        // Add the states to retval as Succesor("flip i j, new_state)
        // new_state has to be a copy of state

        int mode = 1;

        switch (mode) {
            case 1:
                return changeConnectionOperator(networkConf);
            case 2:
                return swapConnectionsOperator(networkConf);
            case 3:
                List ret = changeConnectionOperator(networkConf);
                ret.addAll(swapConnectionsOperator(networkConf));
                return ret;
            default:
                return null;
        }
    }

    private List changeConnectionOperator(IAState networkConf) {
        ArrayList retval = new ArrayList();
        for (int i = 0; i < IAState.getNumSensors(); ++i) {
            ArrayList<ArrayList<Integer>> destinations = networkConf.getAllPosibleDestinations(i);
            ArrayList<Integer> centers = destinations.get(0);
            ArrayList<Integer> sensors = destinations.get(1);

            for (int c = 0; c < centers.size(); ++c) {
                IAState newState = new IAState(networkConf);
                newState.changeConnection(i,c-IAState.getNumCenters());
                retval.add(new Successor("",newState));
            }
            for (int s = 0; s < sensors.size(); ++s) {
                IAState newState = new IAState(networkConf);
                newState.changeConnection(i,s);
                retval.add(new Successor("",newState));
            }
        }
        return retval;
    }

    private List swapConnectionsOperator(IAState networkConf) {
        ArrayList retval = new ArrayList();
        HashSet<ArrayList<Integer>> swaps = networkConf.getAllNonDependantPairsOfSensors();
        for (ArrayList<Integer> swap: swaps) {
            IAState newState = new IAState(networkConf);
            newState.swapConnections(swap.get(0),swap.get(1));
            retval.add(new Successor("",newState));
        }
        return retval;
    }
}
