package teamawesome;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

/**
 * GenericRobot
 * This is the parent class of the different robots
 * This is an abstract class and should never be instantiated as an actual robot
 * turn() is called to exercise the robot for one round of action
 *
 * 25 October 2021
 * Currently this is just a base class all the robots can share so we can take
 * advantage of polymorphism in storing robots. Eventually this class will
 * implement common functions like signaling
 *
 */

abstract public class GenericRobot {
    RobotController rc;
    public GenericRobot(RobotController newRc) {
        rc = newRc;
    }

    abstract void turn() throws GameActionException;
}
