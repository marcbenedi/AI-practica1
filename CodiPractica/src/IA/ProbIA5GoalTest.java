package IA;

import aima.search.framework.GoalTest;

/**
 * Created by bejar on 17/01/17.
 */
public class ProbIA5GoalTest implements GoalTest {

    public boolean isGoalState(Object state){

        return((IAState) state).is_goal();
    }
}
