package IA;


import IA.Red.Centro;
import IA.Red.CentrosDatos;
import IA.Red.Sensor;
import IA.Red.Sensores;

import java.util.*;

import static java.lang.Math.sqrt;

public class IAState {

    //CONSTANTS (FINAL and also static) --------------------------------------------------------------------------------

    private static final int numCenters = 14;
    private static final int numSensors = 125;
    private static final int seed = 1234;
    private static final int maxFlowCenter = 125;
    private static final int inputMaxCenter = 25;
    private static final int inputMaxSensor = 3;
    private static final int notConnected = -numCenters-1;
    //------------------------------------------------------------------------------------------------------------------



    //CLASS VARIABLES (STATIC)------------------------------------------------------------------------------------------

    /*
    * If numCenters = 4 and numSensors = 5
    * ---------------------------------------
    * |0  |1  |2  |3  |4  |5  |6  |7  |8  |9 |//Index of array
    * |0  |1  |2  |3  |0  |1  |2  |3  |4  |5 |//Index of (centers/sensors)
    * |-4 |-3 |-2 |-1 |0  |1  |2  |3  |4  |5 |//Value connectedTo
    * ---------------------------------------
    *
    * Explanation of the value connectedTo(i)
    *
    * When the value v == -numCenters-1 it represents
    * that this SENSOR (i) is NOT connected TO ANYBODY
    *
    * When the value -numCenters - 1 < v < 0 it represents
    * that the SENSOR (i) is connected to
    * the CENTER in the position numCenters+v
    * in the DS centers
    *
    * When the value v>=0 it represents
    * that the SENSOR (i) is connected to
    * the SENSOR in the position v
    * in the DS sensors
    *
    * */

    //Index from 0 to numCenters-1
    private static CentrosDatos centers;
    //Index from 0 to numSensors-1
    private static Sensores sensors;
    //Distance matrix. Rows = numSensors, Columns = numCenter + numSensors. (The first columns reference centers)
    private static double[][] distances;
    //------------------------------------------------------------------------------------------------------------------



    //INDIVIDUAL STATE VARIABLES----------------------------------------------------------------------------------------

    // - -> DataCenter (C)
    // + -> Sensors    (S)
    // -C <= value <= S-1
    // Size = S
    private int[] connectedTo;

    // Size S + C
    private int[] inputConnections;
    // Size S + C
    private int[] inputFlow;
    //Size S + C
    private int[] collectedDataVolume;
    //------------------------------------------------------------------------------------------------------------------


    //TODO: Be sure that the static variables are initialized in the creator function
    //Segons l'Hermes no fa falta perquè generem un estat nou a partir del constructor per copia

    public static int getNumCenters() {
        return numCenters;
    }

    public static int getNumSensors() {
        return numSensors;
    }

    /* Constructor */
    public IAState() {

        //Initializing the problem with IA.Red input
        //Create the CentroDatos ArrayList with a random seed.
        centers = new CentrosDatos(numCenters, seed);
        //Create the Sensores ArrayList with a random seed.
        sensors = new Sensores(numSensors, seed);

        //Initializing the size of the arrays
        connectedTo = new int[numSensors];
        inputConnections = new int[numSensors + numCenters];
        inputFlow = new int[numSensors + numCenters];
        collectedDataVolume = new int[numSensors + numCenters];
        distances = new double[numSensors][numCenters + numSensors];

        //Initializing the value of the CENTERS [0 .. numCenters]
        for (int i = 0; i < numCenters; ++i) {
            collectedDataVolume[i] = maxFlowCenter;
            inputConnections[i] = 0;
            inputFlow[i] = 0;
        }

        //Initializing the value of the SENSORS [numCenters .. numCenters+numSensors] - shift = [0 .. numSensors]
        for (int i = numCenters; i < numSensors+numCenters; ++i) {
            collectedDataVolume[i] = (int) sensors.get(i - numCenters).getCapacidad();
            connectedTo[i - numCenters] = notConnected;
            inputConnections[i] = 0;
            inputFlow[i] = 0;
        }

        initDistanceMatrix();
        //Generate the initial solution 1
        generarSolucioInicial1();

    }

    //Constructor by copy
    public IAState(IAState state) {
        //TODO: Potser s'ha de posar .clone perquè sinó faran referència al mateix array.
        //L'Hermes have tried and it works as it says.
        //Marc: If we don't put clone Hill Climbing ony expands 1 node.
        this.connectedTo = state.connectedTo.clone();
        this.inputConnections = state.inputConnections.clone();
        this.inputFlow = state.inputFlow.clone();
        this.collectedDataVolume = state.collectedDataVolume.clone();
    }

    public boolean is_goal() {
        //Because we are using High Climbing it always return false.
        return false;
    }

    //Updates the incoming flow of a sensor or a center. If the amount of flow is greater than my capacity, the extra
    //flow is lost and I send my max output flow (3*collectedDataValue).
    private void updateFlow(Integer s, Double capacity) {
        //TODO: int capacity = parametre.intValue(); (per evitar tantes crides a intValue()).
        // Base case, I am in a sensor.
        if (s >= 0) {
            //We don't exceed our collectedDataVolume
            if(2* collectedDataVolume[s+numCenters] >= inputFlow[s + numCenters] + capacity.intValue()){
                updateFlow(connectedTo[s], capacity);
                inputFlow[s+numCenters] += capacity.intValue();
            }
            else {
                //We exceed our collectedDataVolume, we send our collectedDataVolume
                //Difference: My maximum output (3*myCollectedValue) - what is was sending you before.
                //3*collectedDataVolume - (inputFlow + myCollectedDataValue)
                //2*collectedDataVolumne - inputFlow
                updateFlow(connectedTo[s], (double) 2*collectedDataVolume[s+numCenters] - inputFlow[s+numCenters]);
                inputFlow[s+numCenters] = 2*collectedDataVolume[s+numCenters];
            }
        }
        // I am in a data center.
        else {
            //We don't exceed
            if(maxFlowCenter >= inputFlow[s + numCenters] + capacity.intValue()){
                inputFlow[s+numCenters] += capacity.intValue();
            }
            else{
                inputFlow[s + numCenters] = maxFlowCenter;
            }
        }
        //In case that there has been an error
        assert inputFlow[s+numCenters] < 0;
    }

    //Returns the index of the nearest NOT connected sensor.
    //If there are not any free sensors the return value is -1.
    private Integer findNearestNotConnectedSensor(Integer s) {
        //TODO: Is really time expensive
        //max = 0 because nothing will be closer to me than myself.
        double max = 0;
        int index_max = -1;
        for (int j = numCenters; j < numSensors + numCenters; ++j) {
            if (max < distances[s][j] && connectedTo[j - numCenters] == notConnected) {
                max = distances[s][j];
                index_max = j - numCenters;
            }
        }
        return index_max;
    }

    //For each center, this function calculates its distance between it and all the sensors.
    private void calculateQueuePerCenter(ArrayList<PriorityQueue<Integer>> volumeDistanceOrderedSensorsPerCenter) {
        int index_center = 0;
        for (Centro c : centers) {
            volumeDistanceOrderedSensorsPerCenter.add(new PriorityQueue<Integer>(new SensorComparator(index_center)));
            for (int i = 0; i < numSensors; ++i) {
                volumeDistanceOrderedSensorsPerCenter.get(index_center).add(i);
            }
            ++index_center;
        }
    }

    //First connect all the sensors of 5MB to the data centers. Secondly those with 2MB to the 5MB and finally the 1MB to 2MB.
    //In case that we are short of any of those sensors we proceed with the next decreasing sensort type.
    private void generarSolucioInicial1() {

        Queue<Integer> this_level = new LinkedList<>();

        //Index from 0 to numCenter-1
        //Integer goes from 0 to numSensors-1
        //Ordered by ascending distance
        ArrayList<PriorityQueue<Integer>> volumeDistanceOrderedSensorsPerCenter = new ArrayList<>();

        calculateQueuePerCenter(volumeDistanceOrderedSensorsPerCenter);

        int index_c = -numCenters;

        for (Centro c : centers) {
            //Poll: Top pop
            //s is the index of the greatest (and nearest) sensor.
            Integer s = volumeDistanceOrderedSensorsPerCenter.get(numCenters + index_c).poll();
            //If there are any sensors and the center has free space.
            while (s != null && inputConnections[numCenters + index_c] < inputMaxCenter) {
                //If s sensor is not connected
                if (connectedTo[s] == notConnected) {
                    connectedTo[s] = index_c;
                    this_level.add(s);
                    inputConnections[numCenters + index_c] += 1;
                    inputFlow[numCenters + index_c] += sensors.get(s).getCapacidad();
                }
                s = volumeDistanceOrderedSensorsPerCenter.get(numCenters + index_c).poll();
            }
            ++index_c;
        }

        //Connecting sensors to sensors.

        //The first level is covered (either the first level is full or there are spare sensors).
        //Then we proceed with the second level, and so forth.

        //available respresents if there are spare sensors.
        boolean available = true;
        //If we have spare sensors (available) and we have some sensors (already connected) to connecto to (this level)
        //TODO: SI this_leve.isEmpty() vol dir que no en queden lliure, ergo available sera FALSE
        while (!this_level.isEmpty() && available) {
            //These sensors are the ones connected to this level.
            Queue<Integer> next_level = new LinkedList<>();
            while (!this_level.isEmpty() && available) {
                Integer s = this_level.poll();
                //We have a space sensor and it has enough space.
                while (s != null && inputConnections[s + numCenters] < inputMaxSensor && available) {
                    Integer i = findNearestNotConnectedSensor(s);
                    // If there is a free sensor to connect to s
                    if (i != -1) {
                        connectedTo[i] = s;
                        next_level.add(i);
                        inputConnections[s + numCenters] += 1;
                        //updateFlow(connectedTo[s], sensors.get(s).getCapacidad());
                        updateFlow(s /*connectedTo[i] = s*/, sensors.get(i).getCapacidad());
                    } else {
                        available = false;
                    }
                }
                this_level = next_level;
            }
        }
    }

    //Fills up the distance matrix. distance[i][j] is the distance between the sensor i and the center/sensor j.
    //0 <= j < numCenters : Centers
    //numCenters <= j < numCenters + numSensors : Sensor (But sensors[j-numCenters]).
    private void initDistanceMatrix() {
        //Rows
        for (int i = 0; i < numSensors; ++i) {
            //Columns
            for (int j = 0; j < numCenters + numSensors; ++j) {
                if (j < numCenters)
                    distances[i][j] = calculateDistance(sensors.get(i), centers.get(j));
                else
                    distances[i][j] = calculateDistance(sensors.get(i), sensors.get(j - numCenters));
            }
        }
    }

    //Calculate the distance between two sensors
    private double calculateDistance(Sensor s1, Sensor s2) {
        return sqrt(Math.pow((s1.getCoordX() - s2.getCoordX()), 2) + Math.pow((s1.getCoordY() - s2.getCoordY()), 2));
    }

    //Calculate the distance between one sensor and one center
    private double calculateDistance(Sensor s, Centro c) {
        return sqrt(Math.pow((s.getCoordX() - c.getCoordX()), 2) + Math.pow((s.getCoordY() - c.getCoordY()), 2));
    }

    //Compare two Sensors first by capacity and after by distance.
    public class SensorComparator implements Comparator<Integer> {
        private int ind_c;

        public SensorComparator(int ind_c) {
            this.ind_c = ind_c;
        }

        public void setId(int ind_c) {
            this.ind_c = ind_c;
        }

        @Override
        public int compare(Integer i1, Integer i2) {
            Sensor s1 = sensors.get(i1);
            Sensor s2 = sensors.get(i2);

            if (s1.getCapacidad() < s2.getCapacidad()) return 1;
            else if (s1.getCapacidad() > s2.getCapacidad()) return -1;
            else {
                //Go to distance matrix and compare the distance
                if (distances[i1][ind_c] < distances[i2][ind_c]) return -1;
                else return 1;
            }
        }
    }

    //-----------------------------------------SUCCESOR GENERATOR-------------------------------------------------------

    //----------------------------------------1: Canviar connexió-------------------------------------------------------

    //Returns a list with two lists containing
    // all the spots (sensors and data centers) that have availability for input connections.
    // where I could be connected. (and I will still be in the solution space)
    public ArrayList<ArrayList<Integer>> getAllPosibleDestinations(int sensor_id) {
        HashSet<Integer> dependant = dependingSensors(sensor_id);
        //centersSpots are centers with spare connections.
        ArrayList<Integer> centersSpots = new ArrayList<>();
        for (int i = 0; i < numCenters; ++i) {
            if (inputConnections[i] < inputMaxCenter) {
                centersSpots.add(i);
            }
        }
        ArrayList<Integer> sensorsSpots = new ArrayList<>();
        for (int i = numCenters; i < numCenters+numSensors; ++i) {
            if (inputConnections[i] < inputMaxSensor) {
                //To avoid loops and If it is not me
                if (!dependant.contains(i-numCenters) && i - numCenters != sensor_id) {
                    sensorsSpots.add(i - numCenters);
                }
            }
        }

        ArrayList<ArrayList<Integer>> result = new ArrayList<>();
        result.add(centersSpots);
        result.add(sensorsSpots);
        return result;
    }

    //method that given a sensor_id returns
    // a set with the sensors which are connected to it directly or indirectly
    private HashSet<Integer> dependingSensors(int sensor_id) {
        HashSet<Integer> depending = new HashSet<>();
        for (int i = 0; i < numSensors; ++i) {
            //If I'm connected to sensor_id
            if (connectedTo[i] == sensor_id) {
                depending.add(i);
                depending.addAll(dependingSensors(i));
            }
        }
        return depending;
    }

    //Changes sensor_id output connection to destination,
    //and does all related work: modify flow, and array modifications
    public void changeConnection(int sensor_id, int destination) {
        // 0.- disconnect sensor_id from previous connection DONE
        // 1.- change sensor_id connectedTo DONE
        // 2.- change destination inputConnections DONE
        // 3.- change destination and previousConnection inputFlow DONE
        // 4.- change destination collectedDataVolume
        // +(EN PRINCIPIO YA LO DEBERIA HACER updateFlow)

        int previousConnection = connectedTo[sensor_id];

        inputConnections[numCenters+previousConnection] -= 1;

        connectedTo[sensor_id] = destination;
        //destination is either a datacenter or sensor. NEGATIVE FOR CENTERS NEED TO BE MANAGED BY THE INVOKER

        inputConnections[numCenters+destination] += 1;

        Double sensorOutputFlow  = inputFlow[numCenters+sensor_id]+sensors.get(sensor_id).getCapacidad();

        updateFlow(destination, sensorOutputFlow);

        updateFlow(previousConnection, -sensorOutputFlow);
    }

    //------------------------------------2: SWAP CONNECTIONS-----------------------------------------------------------

    //Returns all non-dependant pairs of sensors in a set of arraylist in which the first component is always
    //the sensor with the lower id
    public HashSet<ArrayList<Integer>> getAllNonDependantPairsOfSensors() {
        ArrayList<ArrayList<Integer>> pairs = new ArrayList<>();
        for (int i = 0; i < numSensors; ++i) {
            HashSet<Integer> dependant = dependingSensors(i);
            for (int j = 0; j < numSensors; ++j) {
                if (!dependant.contains(j) && i != j) {
                    ArrayList<Integer> pair = new ArrayList<>();
                    if (i < j) {
                        pair.add(i);
                        pair.add(j);
                    }
                    else {
                        pair.add(j);
                        pair.add(i);
                    }
                    pairs.add(pair);
                }
            }
        }
        return new HashSet<>(pairs);
    }


    //Pre: sensors are non dependant
    public void swapConnections(int sensor_id_1, int sensor_id_2) {
        // 0.- Change both connectedTo
        // 1.- Update flow on both

        //TODO: Maybe it can be done with changeConnection()
        //Problem: If all the sensors are full we can not disconnect and then connect to the other before disconnecting
        //the other.

        int previousConnectionSensor1 = connectedTo[sensor_id_1];
        int previousConnectionSensor2 = connectedTo[sensor_id_2];

        connectedTo[sensor_id_1] = previousConnectionSensor2;
        connectedTo[sensor_id_2] = previousConnectionSensor1;

        Sensor sensor1 = sensors.get(sensor_id_1);
        Sensor sensor2 = sensors.get(sensor_id_2);

        Double sensorOneOutputFlow = inputFlow[numCenters+sensor_id_1]+sensor1.getCapacidad();
        Double sensorTwoOutputFlow = inputFlow[numCenters+sensor_id_2]+sensor2.getCapacidad();

        Double amountToUpdateOnPreviousConnectionOne = sensorTwoOutputFlow - sensorOneOutputFlow;
        Double amountToUpdateOnPreviousConnectionTwo = sensorOneOutputFlow - sensorTwoOutputFlow;

        updateFlow(previousConnectionSensor1, amountToUpdateOnPreviousConnectionOne);
        updateFlow(previousConnectionSensor2, amountToUpdateOnPreviousConnectionTwo);
    }

    //-------------------------------------HEURISTIC FUNCTIONS----------------------------------------------------------
    private int computeCost(){

        int sum = 0;

        for(int i = 0; i < numSensors; ++i) {
            sum += (int) Math.pow(distances[i][connectedTo[i] + numCenters], 2) * (inputFlow[i + numCenters] + sensors.get(i).getCapacidad());
        }
        return sum;
    }

    private int computeArrivalData(){

        int sum = 0;

        for(int i = 0; i < numCenters; ++i)
            sum += inputFlow[i];

        assert sum != 0;
        return sum;
    }

    //min (cost/dades) ~= max(dades/cost)
    public double heuristic1() {
        //TODO: There is a overflow problem with the double variables.
        double x = computeArrivalData();
        int y = computeCost();
        //System.out.println("AAAAAAAAAAAAAA");
        //System.out.println(x);
        //System.out.println(y);
        //TODO: Assert is not working. x = -1;
        assert x >= 0;
        assert y >= 0;
        return x/y;
        //return computeArrivalData()/computeCost();
        //return computeCost()/computeArrivalData();
    }

    //------------------------------------------DEBUGGING FUNCTIONS-----------------------------------------------------
    public int printTotalCostCalculation(){
        int sum = 0;
        System.out.println("INI CALCULPRINT");
        for(int i = 0; i < numSensors; ++i) {
            System.out.println("la distancia és de " +distances[i][connectedTo[i] + numCenters]+ " entre "+ i + " "+ connectedTo[i]);
            sum += Math.pow(distances[i][connectedTo[i] + numCenters], 2) * (inputFlow[i + numCenters] + sensors.get(i).getCapacidad());
            System.out.println("sum = " + sum);
        }
        return sum;
    }

    public void printState(){
        //100x100 geografic area
        for (double i = 0.0; i < 100.0; ++i){
            for(double j = 0.0; j < 100.0; ++j){
                //Search if there is a sensor or center in that position
                boolean found = false;
                for(int c = 0; c < numCenters && !found; ++c){
                    double x = centers.get(c).getCoordX();
                    double y = centers.get(c).getCoordY();
                    if(x == j && y == i){
                        found = true;
                        System.out.print(c-numCenters);
                    }
                }
                for (int s = 0; s < numSensors && !found; ++s){
                    double x = sensors.get(s).getCoordX();
                    double y = sensors.get(s).getCoordY();
                    if(x == j && y == i){
                        found = true;
                        System.out.print(s);
                    }
                }
                if(!found) System.out.print("·");
            }
            System.out.println();
        }
    }

}
