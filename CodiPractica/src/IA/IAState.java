package IA;


import IA.Red.CentrosDatos;
import IA.Red.Sensores;

import java.util.ArrayList;

public class IAState {

    private static CentrosDatos centers;
    private static Sensores sensors;

    private static final int numCenters = 4;
    private static final int numSensors = 150;
    private static final int seed = 1234;
    private static final int maxFlowCenter = 125;

    // - -> DataCenter (C)
    // + -> Sensors    (S)
    // -C <= value <= S-1
    // Mida S
    private int[] connectedTo;

    //Mida S + C
    private int[] inputConnections;
    //Mida S + C
    private int[] inputFlow;
    //Mida S + C
    private int[] maxFlow;


    /* Constructor */
    public IAState() {

        //Create the CentroDatos ArrayList with a random seed.
        centers = new CentrosDatos(numCenters,seed);

        //Create the Sensores ArrayList with a random seed.
        sensors = new Sensores(numSensors, seed);

        connectedTo = new int[numSensors];
        inputConnections = new int[numSensors+numCenters];
        inputFlow = new int[numSensors+numCenters];
        maxFlow = new int[numSensors+numCenters];

        for (int i = 0; i < numCenters; ++i){
            maxFlow[i] = maxFlowCenter;
            inputConnections[i] = 0;
            inputFlow[i] = 0;
        }

        for (int i = numCenters; i < numSensors; ++i){
            maxFlow[i] = (int) sensors.get(i-numCenters).getCapacidad();
            connectedTo[i-numCenters] = 0;
            inputConnections[i] = 0;
            inputFlow[i] = 0;
        }

        //generarSolucioInicial();

    }

    public double heuristic(){
        // compute the number of coins out of place respect to solution
        return 0;
    }

     public boolean is_goal(){
         // compute if board = solution
         return false;
     }


}
