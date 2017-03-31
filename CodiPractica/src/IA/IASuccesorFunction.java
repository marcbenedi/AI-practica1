package IA;

import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class IASuccesorFunction implements SuccessorFunction{

    private static int mode;

    public IASuccesorFunction(int op_id) {
        mode = op_id;
    }

    public List getSuccessors(Object state){
        IAState networkConf = (IAState) state;

        // Apply the selected (mode) operator and generate new states
        // Add the states to retval as Succesor("flip i j, new state)
        // new_state has to be a copy of state
        networkConf.printStateCSV();

        switch (mode) {
            case 1:
                return changeConnectionOperator(networkConf);
            case 2:
                return swapConnectionsOperator(networkConf);
            case 3:
                //Swap+ mode
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
                newState.changeConnection(i,centers.get(c)-IAState.getNumCenters());
                retval.add(new Successor("",newState));
            }
            for (int s = 0; s < sensors.size(); ++s) {
                IAState newState = new IAState(networkConf);
                newState.changeConnection(i,sensors.get(s));
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
