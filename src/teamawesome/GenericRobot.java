package teamawesome;

import battlecode.common.Direction;
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

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    protected Direction randomDirection() {
        return RobotPlayer.directions[(int) (Math.random() * RobotPlayer.directions.length)];
    }



    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    protected boolean tryMove(Direction dir) throws GameActionException {
        System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }
}
