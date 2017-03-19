package IA;


import IA.Red.Centro;
import IA.Red.CentrosDatos;
import IA.Red.Sensor;
import IA.Red.Sensores;

import java.util.*;

import static java.lang.Math.sqrt;

public class IAState {

    private static CentrosDatos centers;
    private static Sensores sensors;

    private static ArrayList<PriorityQueue<Integer>> volumeDistanceOrderedSensorsPerCenter;

    //matriu de distàncies de numSensors files i numSensors+numCenters columnes (les primeres columnes corresponen a centres)
    //TODO: Passar a anglès
    private static double[][] distances;

    private static final int numCenters = 4;
    private static final int numSensors = 150;
    private static final int seed = 1234;
    private static final int maxFlowCenter = 125;
    private static final int inputMaxCenter = 25;
    private static final int inputMaxSensor = 3;

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
            connectedTo[i-numCenters] = -numCenters-1;
            inputConnections[i] = 0;
            inputFlow[i] = 0;
        }

        calcularMatriuDistancies();
        //Generate the initial solution 1
        generarSolucioInicial1();

    }

    public double heuristic(){
        // compute the number of coins out of place respect to solution
        return 0;
    }

     public boolean is_goal(){
         // compute if board = solution
         return false;
     }

     private void updateFlow(Integer s, Double capacity){
         // Base case, i'm in one of the data centers.
         if (s >= 0) {
             inputFlow[s+numCenters] += capacity;
             updateFlow(connectedTo[s],capacity);
         }
         else {
             inputFlow[s+numCenters] += capacity;
         }
     }

     private Integer findNearestNotConnectedSensor(Integer s){
         
     }

     //First connect all the sensors of 5MB to the data centers. Secondly those with 2MB to the 5MB and finally the 1MB to 2MB.
     private void generarSolucioInicial1(){
         int index_c = 0;

         Queue<Integer> this_level = new LinkedList<>();

         for (Centro c: centers) {
             Integer s = volumeDistanceOrderedSensorsPerCenter.get(index_c).poll();
             while(inputConnections[index_c] < inputMaxCenter && s != null){
                 //If it is not connected
                 if(connectedTo[s] == -numCenters-1){
                     connectedTo[s] = index_c;
                     this_level.add(s);
                     inputConnections[index_c] += 1;
                     inputFlow[index_c] += sensors.get(s).getCapacidad();
                 }
                 s = volumeDistanceOrderedSensorsPerCenter.get(index_c).poll();
             }
             ++index_c;
         }

         //The first level is covered

         while (!this_level.isEmpty()){
             Queue<Integer> next_level = new LinkedList<>();
             while (!this_level.isEmpty()){
                 Integer s = this_level.poll();
                 while (inputConnections[s+numCenters] < inputMaxSensor ){
                     Integer i = findNearestNotConnectedSensor(s);
                     connectedTo[i] = s;
                     next_level.add(i);
                     inputConnections[s+numCenters] += 1;
                     updateFlow(connectedTo[s], sensors.get(s).getCapacidad());
                 }
                 this_level = next_level;
             }
         }
     }

     //TODO: Canviar a angles
     private void calcularMatriuDistancies() {
         for (int i = 0; i < numSensors; ++i) {
             for (int j = 0; j < numCenters+numSensors; ++j) {
                 if (j < numCenters)
                    distances[i][j] = calculateDistance(sensors.get(i),centers.get(j));
                 else
                     distances[i][j] = calculateDistance(sensors.get(i),sensors.get(j-numCenters));
             }
         }
     }

     private void calculateQueuePerCenter(){
         volumeDistanceOrderedSensorsPerCenter = new ArrayList<>();
         int index_center = 0;
         for (Centro c : centers) {
             volumeDistanceOrderedSensorsPerCenter.add(new PriorityQueue<Integer>(new SensorComparator(index_center)));
             for (int i = 0; i < numSensors; ++i){
                 volumeDistanceOrderedSensorsPerCenter.get(index_center).add(i);
             }
             ++index_center;
         }
     }

     private double calculateDistance(Sensor s1,Sensor s2){
         return sqrt((s1.getCoordX()-s2.getCoordX())^2 + (s1.getCoordY()-s2.getCoordY())^2);
     }

     private double calculateDistance(Sensor s, Centro c){
         return sqrt((s.getCoordX()-c.getCoordX())^2 + (s.getCoordY()-c.getCoordY())^2);
     }

     private class Triplet {
         private int sensor_id;
         private int sensor2_id;
         private boolean sensor_to_sensor;
         private int center_id;
         private double distance;

         public Triplet(int sid ,int cid, double dist) {
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

         public double getDistance() {return distance;}

         public int getSensor_id() {return sensor_id;}

         public int getCenter_id() {return center_id;}

         public boolean getSensor_to_sensor() {return sensor_to_sensor;}

         public int getSensor2_id() {return sensor2_id;}
     }

     public class DistanceComparator implements Comparator<Triplet> {
         @Override
         public int compare(Triplet triplet, Triplet t1) {
             if (triplet.getDistance() < t1.getDistance()) return -1;
             else if (triplet.getDistance() > t1.getDistance()) return 1;
             else return 0;
         }
     }

     public class SensorComparator implements Comparator<Integer> {
         private int ind_c;

         public SensorComparator(int ind_c){
             this.ind_c = ind_c;
         }

         public void setId(int ind_c){
             this.ind_c = ind_c;
         }

         @Override
         public int compare(Integer i1, Integer i2) {
             Sensor s1 = sensors.get(i1);
             Sensor s2 = sensors.get(i2);

             if (s1.getCapacidad() < s2.getCapacidad()) return 1;
             else if (s1.getCapacidad() > s2.getCapacidad()) return -1;
             else {
                 //Accedir a la matriu de distancies i comparar distancia
                 if(distances[i1][ind_c] < distances[i2][ind_c]) return -1;
                 else return 1;
             }
         }
     }

}
