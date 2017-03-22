import IA.IAState;

/**
 * Created by marcbenedi on 17/03/17.
 */
public class Main {


    //static int [] prob = new int []{1 ,0, 1, 1, 0};
    //static int [] sol = new int[]{1, 1, 0, 1, 0};


    public static void main(String[] args){

        System.out.println("Createing IAState");

        IAState myState = new IAState();

        System.out.println("Calling the heuristic function");
        Double t = myState.heuristic1();
        System.out.println(t);
    }
}
