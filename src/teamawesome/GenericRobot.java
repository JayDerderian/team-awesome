package teamawesome;
import static teamawesome.FlagConstants.*;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import scala.Int;

import java.util.Hashtable;

/**
 * GenericRobot
 * This is the parent class of the different robots
 * This is an abstract class and should never be instantiated as an actual robot
 * turn() is called to exercise the robot for one round of action
 *
 * 25 October 2021
 * Currently this is just a base class all the robots can share so we can take
 * advantage of polymorphism in storing robots. Eventually this class will
 * implement common functions like signaling. For now it contains the utility
 * functions from the examplefuncsplayer's RobotPlayer
 *
 */

abstract public class GenericRobot {
    RobotController rc;
    public GenericRobot(RobotController newRc) {
        rc = newRc;
    }

    /**
     * Abstract method for actuating the robot for one turn
     * Must be implemented by child class
     * @throws GameActionException
     */
    abstract void turn() throws GameActionException;

    /**
     * Borrowed from examplefuncsplayer, Returns a random Direction.
     *
     * @return a random Direction
     */
    protected Direction randomDirection() {
        return RobotPlayer.directions[(int) (Math.random() * RobotPlayer.directions.length)];
    }


    /**
     * Supply a given robot's id, and pass the currant instance of the
     * robot controller.
     *
     * This method will attempt to retrieve the flag of the given bot and
     * pass it to the helper parser method. A small hashtable will be returned
     * who's values can be searched for using the FlagConstants. For example,
     * if you want to see what you got back you'll probably have to iterate
     * through the FlagConstants until you get a result.
     *
     * If there's an error at any point, then a table containing the key ERROR
     * will be returned.
     *
     * @throws GameActionException
     * @return a Hashtable
     **/
    protected static Hashtable<Integer, Integer> getFlag(RobotController rc, int id) throws GameActionException {
        // hash table containing all flag info.
        // see FlagConstants.java for a breakdown on entries.
        Hashtable<Integer, Integer> res = new Hashtable<>();
        // try to get flag from a given bot
        if (rc.canGetFlag(id)) {
            // get flag from given bot (id)
            int flag = rc.getFlag(id);
            // convert flag to string for parsing purposes
            String flagStr = String.valueOf(flag);
            // is this one of our flags? get first two
            // characters and convert back to an int
            // ... i know, it's silly.
            int firstTwo = Integer.parseInt(flagStr.substring(0,1));
            if (firstTwo != PASSWORD){
                System.out.print("Not one of our flags!");
                res.put(ERROR, 0);
                return res;
            }
            else{
                // call helper.
                // returns its own hashtable
                return parseFlag(flagStr);
            }
        }
        else {
            System.out.print("Could not get flag for a nearby bot!");
            res.put(ERROR, 0);
            return res;
        }
    }

    protected static Hashtable<Integer, Integer> parseFlag (String flagStr){
        // results table
        Hashtable<Integer, Integer> results = new Hashtable<>();
        // This is an alert! What kind is it?
        if (flagStr.length() == 4){
            // remove first two digits (pw), then
            // determine alert type
            String alert = flagStr.substring(2,3);
            int type = Character.getNumericValue(alert.charAt(0));
            // Neutral EC found!
            if(type == 1){
                // get EC location, then append to hashtable
                int loc = alert.charAt(1);
                results.put(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG, loc);
            }
            // Need help!
            else if(type == 2){
                // add location
                int loc = alert.charAt(1);
                results.put(NEED_HELP, loc);
            }
        }
        // This is enemy info!
        else if (flagStr.length() == 6){

            // NOTE might want to modify this to allow for the
            // actual (x,y) coordinates returned from

            // This is enemy info! What kind is it?
            // remove first two digits (pw), then
            // determine enemy info
            String enemyInfo = flagStr.substring(2,5);
            int type = Character.getNumericValue(enemyInfo.charAt(0));
            // Politician spotted!
            if(type == 1){
                // get conviction level and location,
                // then append to table
                int conv = Integer.parseInt(enemyInfo.substring(1,2));
                int loc = Integer.parseInt(enemyInfo.substring(3));
                results.put(ENEMY_POLITICIAN_FLAG, conv);
                results.put(LOCATION, loc);
            }
            // Slanderer spotted!
            else if(type == 2){
                int loc = Integer.parseInt(enemyInfo.substring(3));
                results.put(ENEMY_SLANDERER_NEARBY_FLAG, loc);
            }
            // Muckraker spotted!
            else if(type == 3){
                int loc = Integer.parseInt(enemyInfo.substring(3));
                results.put(ENEMY_MUCKRAKER_NEARBY_FLAG, loc);
            }
        }
        else{
            System.out.print("Unable tp parse flag!");
            results.put(ERROR,0);
        }
        return results;
    }


    /**
     * Borrowed from examplefuncsplayer, Attempts to move in a given direction.
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
