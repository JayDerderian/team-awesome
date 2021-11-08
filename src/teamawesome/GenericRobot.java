package teamawesome;
import static teamawesome.FlagConstants.*;

import battlecode.common.*;
import scala.Int;

import java.util.Hashtable;
import java.lang.Math;

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
     * Takes a flag type (ERROR or ALERT), the base flag (see FlagConstants.java),
     * and optional conviction level. Enter 0 for conv if you want to set an alert.
     *
     * @return Integer
     */
    protected Integer makeFlag(int type, int flag, int conv) {
        int newFlag = 0;
        String pw = Integer.toString(PASSWORD);
        // 3 digit flags
        if (type == ALERT){
            if (flag == NEED_HELP){
                String nh = Integer.toString(NEED_HELP);
                String flagStr = pw + nh;
                newFlag = Integer.parseInt(flagStr);
            }
            else if (flag == GO_HERE){
                String gh = Integer.toString(GO_HERE);
                String flagStr = pw + gh;
                newFlag = Integer.parseInt(flagStr);
            }
            else if (flag == NEUTRAL_ENLIGHTENMENT_CENTER_FLAG){
                String nec = Integer.toString(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG);
                String flagStr = pw + nec;
                newFlag = Integer.parseInt(flagStr);
            }
        }
        // 5 digit flags
        else if (type == ENEMY_INFO){
            if(flag == ENEMY_POLITICIAN_FLAG){
                String EF = Integer.toString(ENEMY_POLITICIAN_FLAG + conv);
                String flagStr = pw + EF;
                newFlag = Integer.parseInt(flagStr);
            }
            else if(flag == ENEMY_SLANDERER_NEARBY_FLAG){
                String EF = Integer.toString(ENEMY_SLANDERER_NEARBY_FLAG + conv);
                String flagStr = pw + EF;
                newFlag = Integer.parseInt(flagStr);
            }
            else if (flag == ENEMY_ENLIGHTENMENT_CENTER_FLAG){
                String EF = Integer.toString(ENEMY_ENLIGHTENMENT_CENTER_FLAG + conv);
                String flagStr = pw + EF;
                newFlag = Integer.parseInt(flagStr);
            }
        }
        // Uh oh!!
        if(newFlag == 0){
            return ERROR;
        }
        return newFlag;
    }

    /**
     * Supply a given robot's id, and this method will attempt to retrieve
     * the flag of the given bot and pass it to the helper parser method.
     *
     * A small hashtable will be returned who's values can be searched for
     * using the FlagConstants as keys. Each flag/key will have an associated
     * MapLocation value.
     *
     * If there's an error at any point, then a table containing the key ERROR
     * will be returned.
     *
     * @param id
     * @return HashTable
     **/
    protected Hashtable<Integer, MapLocation> retrieveFlag (int id) throws GameActionException {
        // hash table containing all flag info.
        // see FlagConstants.java for a breakdown on entries.
        Hashtable<Integer, MapLocation> res = new Hashtable<>();
        // try to get flag from a given bot
        if (rc.canSenseRobot(id)) {
            RobotInfo info = rc.senseRobot(id);
            int flag = rc.getFlag(info.getID());
            // make sure this is one of ours!
            if(isOurs(flag))
                return parseFlag(info, flag);
            else{
                // add our own location since the table requires a MapLocation
                System.out.println("Could not retrieve flag!");
                res.put(ERROR, rc.getLocation());
            }
        }
        else{
            System.out.println("Could not retrieve flag!");
            res.put(ERROR, rc.getLocation());
        }
        return res;
    }

    protected Hashtable <Integer, MapLocation> parseFlag(RobotInfo info, int flagOrig){
        Hashtable<Integer, MapLocation> res = new Hashtable<>();
        int len = countDigis(flagOrig);
        // NOTE: this is redundant if parseFlag is called from retrieveFlag
        // this is here in case parseFlag is called separately.
        if (!isOurs(flagOrig)) {
            res.put(ERROR, rc.getLocation());
            return res;
        }
        // this is an alert!
        if (len == 3){
            // remove first two digits, then test against constants
            int flag = flagOrig % 10;
            if (flag == NEUTRAL_ENLIGHTENMENT_CENTER_FLAG)
                res.put(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG, info.getLocation());
            else if (flag == NEED_HELP)
                res.put(NEED_HELP, info.getLocation());
            else
                res.put(ERROR, rc.getLocation());
        }
        // this is enemy info!
        else if (len == 5){
            // remove first two digits, then test against constants
            int flag = flagOrig % 1000;
            if(flag == ENEMY_ENLIGHTENMENT_CENTER_FLAG)
                res.put(ENEMY_ENLIGHTENMENT_CENTER_FLAG, info.getLocation());
            // enemy politician!
            if(flag/100 == 1)
                res.put(ENEMY_POLITICIAN_FLAG, info.getLocation());
            // enemy slanderer!
            else if(flag/100 == 2)
                res.put(ENEMY_SLANDERER_NEARBY_FLAG, info.getLocation());
            // enemy muckraker!
            else if (flag/100 == 3)
                res.put(ENEMY_MUCKRAKER_NEARBY_FLAG, info.getLocation());
            else
                res.put(ERROR, rc.getLocation());
        }
        return res;
    }

    // Lil' helpers
    private Integer countDigis(int number){
        int count = 0;
        for(; number !=0; number/=10, ++count){}
        return count;
    }

    private Boolean isOurs(int flag){
        int len = countDigis(flag);
        if (len > 5 || len == 4 || len < 3) {
            System.out.print("Not one of our flags!");
            return false;
        }
        /*
         * PASSWORD CHECK!
         *
         * remove trailing digits by using n /= 10^(n-k), where n is total number of digits,
         * and k is the number of digits to remove. since n is unknown until we actually get a
         * flag, we have to count the total digits since our flags are either 3 or 5 digits long
         *
         * ideally body will either equal 10 or 1000
         *
         */
        int firstTwo = flag / (int) Math.pow(10,(len-2));
        if (firstTwo != PASSWORD)
            return false;
        return true;
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
