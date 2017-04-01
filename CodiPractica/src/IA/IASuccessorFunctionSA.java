package IA;

import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class IASuccessorFunctionSA implements SuccessorFunction {

    private IAState networkConf;
    @Override
    public List getSuccessors(Object state) {
         networkConf = (IAState) state;

         //Para el conjunto de operadores swap+
//         if (myRandomOperator.nextInt()%2 == 0) {
//             retVal =  changeConnection();
//         }
//         else {
//             retVal = swapConnection();
//         }

        //Como hemos escogido el conjunto de operadores 1  { Change Connection }
         return changeConnection();
    }

    private ArrayList swapConnection() {
        Random myRandom=new Random();
        int sensOne = myRandom.nextInt(IAState.getNumSensors());
        HashSet<Integer> depOne = networkConf.dependingSensors(sensOne);

        int sensTwo;
        HashSet<Integer> depTwo;

        do {
            sensTwo = myRandom.nextInt(IAState.getNumSensors());
            depTwo = networkConf.dependingSensors(sensTwo);
        } while (sensOne == sensTwo || depOne.contains(sensTwo) && depTwo.contains(sensOne));

        IAState newState = new IAState(networkConf);

        newState.swapConnections(sensOne,sensTwo);

        ArrayList retVal = new ArrayList();
        retVal.add(new Successor("",newState));

        return retVal;
    }

    private ArrayList changeConnection() {

        Random myRandom=new Random();
        int randomSensor = myRandom.nextInt(IAState.getNumSensors());

        ArrayList<ArrayList<Integer>> possibleDestinations = networkConf.getAllPosibleDestinations(randomSensor);

        ArrayList<Integer> posCent = possibleDestinations.get(0);
        ArrayList<Integer> posSens = possibleDestinations.get(1);

        int randomDestination = myRandom.nextInt(posCent.size()+posSens.size());

        IAState newState = new IAState(networkConf);
        if (randomDestination < posCent.size()) {
            newState.changeConnection(randomSensor,posCent.get(randomDestination)-IAState.getNumCenters());
        }
        else  {
            newState.changeConnection(randomSensor,posSens.get(randomDestination-posCent.size()));
        }

        ArrayList retVal = new ArrayList();
        retVal.add(new Successor("",newState));

        return retVal;
    }
}
