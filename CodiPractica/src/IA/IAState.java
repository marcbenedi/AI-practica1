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

    //Index from 0 to numCenter-1
    //Integer goes from 0 to numSensors-1
    //Ordered by ascending distance
    private static ArrayList<PriorityQueue<Integer>> volumeDistanceOrderedSensorsPerCenter;

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

        //Initializing the value
        for (int i = 0; i < numCenters; ++i) {
            collectedDataVolume[i] = maxFlowCenter;
            inputConnections[i] = 0;
            inputFlow[i] = 0;
        }

        for (int i = numCenters; i < numSensors; ++i) { //TODO: BUCLE NO DEBERIA IR HASTA numS+numC ????
            collectedDataVolume[i] = (int) sensors.get(i - numCenters).getCapacidad();
            connectedTo[i - numCenters] = notConnected;
            inputConnections[i] = 0;
            inputFlow[i] = 0;
        }

        calculateDistanceMatrix();
        //Generate the initial solution 1
        generarSolucioInicial1();

    }

    //This method initializes all the static variables
    public static void initClass(){
        //TODO: Be sure that the static variables are initialized in the creator function
        //Because if we do this in the creator object, we will be doing the same every time we apply some operator
    }

    public double heuristic() {
        // compute the number of coins out of place respect to solution
        return 0;
    }

    public boolean is_goal() {
        // compute if board = solution
        return false;
    }

    //Updates the incoming flow of a sensor or a center. If the amount of flow is greater than my capacity, the extra
    //flow is lost and I send my max output flow + my collected data value.
    private void updateFlow(Integer s, Double capacity) {
        // Base case, I am in a sensor.
        if (s >= 0) {

            //We don't exceed our collectedDataVolume
            if(2* collectedDataVolume[s+numCenters] >= inputFlow[s + numCenters] + capacity.intValue()){
                inputFlow[s+numCenters] += capacity.intValue();
                updateFlow(connectedTo[s], capacity);
            }
            else {
                //We exceed our collectedDataVolume, we send our collectedDataVolume
                updateFlow(connectedTo[s], (double) 2*collectedDataVolume[s+numCenters] - inputFlow[s+numCenters]);
                inputFlow[s+numCenters] = 2*collectedDataVolume[s+numCenters];
            }

            //inputFlow[s + numCenters] = min(inputFlow[s + numCenters] + capacity.intValue(), (int) sensors.get(s).getCapacidad()*2);
            //updateFlow(connectedTo[s], capacity);
        }
        // I am in a data center.
        else {
            //TODO: Mirar limit del datacenter
            inputFlow[s + numCenters] += capacity;
        }
    }

    //Returns the index of the nearest NOT connected sensor.
    //If there are not any free sensors the return value is -1.
    private Integer findNearestNotConnectedSensor(Integer s) {
        double max = -1;
        int index_max = -1;
        for (int j = numCenters; j < numSensors + numCenters; ++j) {
            if (max < distances[s][j] && connectedTo[j - numCenters] == notConnected) {
                max = distances[s][j];
                index_max = j - numCenters;
            }
        }
        return index_max;
    }

    //First connect all the sensors of 5MB to the data centers. Secondly those with 2MB to the 5MB and finally the 1MB to 2MB.
    private void generarSolucioInicial1() {

        Queue<Integer> this_level = new LinkedList<>();
        calculateQueuePerCenter();

        int index_c = -numCenters;

        for (Centro c : centers) {
            Integer s = volumeDistanceOrderedSensorsPerCenter.get(numCenters + index_c).poll();
            while (s != null && inputConnections[numCenters + index_c] < inputMaxCenter) {
                //If it is not connected
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

        //The first level is covered

        boolean available = true;
        while (!this_level.isEmpty() && available) {
            Queue<Integer> next_level = new LinkedList<>();
            while (!this_level.isEmpty() && available) {
                Integer s = this_level.poll();
                while (s != null && inputConnections[s + numCenters] < inputMaxSensor && available) {
                    Integer i = findNearestNotConnectedSensor(s);
                    // If there is a free sensor to connect to s
                    if (i != -1) {
                        connectedTo[i] = s;
                        next_level.add(i);
                        inputConnections[s + numCenters] += 1;
                        updateFlow(connectedTo[s], sensors.get(s).getCapacidad());
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
    private void calculateDistanceMatrix() {
        for (int i = 0; i < numSensors; ++i) {
            for (int j = 0; j < numCenters + numSensors; ++j) {
                if (j < numCenters)
                    distances[i][j] = calculateDistance(sensors.get(i), centers.get(j));
                else
                    distances[i][j] = calculateDistance(sensors.get(i), sensors.get(j - numCenters));
            }
        }
    }

    //For each center, this function calculates its distance between it and all the sensors.
    private void calculateQueuePerCenter() {
        volumeDistanceOrderedSensorsPerCenter = new ArrayList<>();
        int index_center = 0;
        for (Centro c : centers) {
            volumeDistanceOrderedSensorsPerCenter.add(new PriorityQueue<Integer>(new SensorComparator(index_center)));
            for (int i = 0; i < numSensors; ++i) {
                volumeDistanceOrderedSensorsPerCenter.get(index_center).add(i);
            }
            ++index_center;
        }
    }

    //Calculate the distance between two sensors
    private double calculateDistance(Sensor s1, Sensor s2) {
        return sqrt((s1.getCoordX() - s2.getCoordX()) ^ 2 + (s1.getCoordY() - s2.getCoordY()) ^ 2);
    }

    //Calculate the distance between one sensor and one center
    private double calculateDistance(Sensor s, Centro c) {
        return sqrt((s.getCoordX() - c.getCoordX()) ^ 2 + (s.getCoordY() - c.getCoordY()) ^ 2);
    }

    //TODO: Are we using it? Or can we delete it?
    private class Triplet {
        private int sensor_id;
        private int sensor2_id;
        private boolean sensor_to_sensor;
        private int center_id;
        private double distance;

        public Triplet(int sid, int cid, double dist) {
            sensor_id = sid;
            center_id = cid;
            distance = dist;
            this.sensor_to_sensor = false;
        }

        public Triplet(int sid, int sid2, boolean sensor_to_sensor, double dist) {
            sensor_id = sid;
            sensor2_id = sid2;
            distance = dist;
            this.sensor_to_sensor = true;
        }

        public double getDistance() {
            return distance;
        }

        public int getSensor_id() {
            return sensor_id;
        }

        public int getCenter_id() {
            return center_id;
        }

        public boolean getSensor_to_sensor() {
            return sensor_to_sensor;
        }

        public int getSensor2_id() {
            return sensor2_id;
        }
    }

    //TODO: Are we using it? Or can we delete it?
    public class DistanceComparator implements Comparator<Triplet> {
        @Override
        public int compare(Triplet triplet, Triplet t1) {
            if (triplet.getDistance() < t1.getDistance()) return -1;
            else if (triplet.getDistance() > t1.getDistance()) return 1;
            else return 0;
        }
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

    // _______________ SUCCESOR GENERATOR

    // ________________ 1: Canviar connexió ___________________________

    //Returns a list with two lists containing
    // all the spots (sensors and data centers) that have availability for input connections.

    public ArrayList getAllPosibleDestinations(int sensor_id) {
        HashSet<Integer> dependant = dependingSensors(sensor_id);
        ArrayList<Integer> centersSpots = new ArrayList<>();
        for (int i = 0; i < numCenters; ++i) {
            if (inputConnections[i] < inputMaxCenter) {
                centersSpots.add(i);
            }
        }
        ArrayList<Integer> sensorsSpots = new ArrayList<>();
        for (int i = numCenters; i < numCenters+numSensors; ++i) {
            if (inputConnections[i] < inputMaxSensor) {
                if (!dependant.contains(i-numCenters)) {
                    if (i - numCenters != sensor_id) {
                        sensorsSpots.add(i - numCenters);
                    }
                }
            }
        }
        ArrayList<ArrayList> result = new ArrayList<>();
        result.add(centersSpots);
        result.add(sensorsSpots);
        return result;
    }

    //method that given a sensor_id returns
    // a set with the sensors which are connected to it directly or indirectly
    private HashSet<Integer> dependingSensors(int sensor_id) {
        HashSet<Integer> depending = new HashSet<Integer>();
        for (int i = 0; i < connectedTo.length; ++i) {
            if (connectedTo[i] == numCenters+sensor_id) {
                depending.add(i);
                depending.addAll(dependingSensors(i));
            }
        }
        return depending;
    }

    //Changes sensor_id output connection to destination,
    //and does all related work: modify flow, and array modifications
    public void changeConnection(int sensor_id, int destination) {
        // 1.- change sensor_id connectedTo DONE
        // 2.- change destination inputConnections
        // 3.- change destination inputFlow
        // 4.- change destination collectedDataVolume

        connectedTo[numCenters+sensor_id] = destination;
        //destination is either a datacenter or sensor. NEGATIVE NEED TO BE MANAGED BY THE INVOKER


        int indice_dataCenter = numCenters+destination; //TODO: Revisar si se calcula bien el índice

        if (destination < 0) {
            inputConnections[indice_dataCenter] += 1;
        }
        else { //destination is another sensor
            inputConnections[numCenters+destination] += 1;
        }

    }

}
