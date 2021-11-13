package teamawesome;

import static teamawesome.FlagConstants.*;

import battlecode.common.*;
import scala.Int;

import java.util.HashMap;
import java.lang.Math;
import java.util.List;

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
    public int makeFlag(int flag, int conv) {
        String pw = Integer.toString(PASSWORD);
        int newFlag = 0;
        // 3 digit flags
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
            String nec = Integer.toString(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG + conv);
            String flagStr = pw + nec;
            newFlag = Integer.parseInt(flagStr);
        }
        // 5 digit flags
        else if (flag == ENEMY_POLITICIAN_FLAG){
            String EF = Integer.toString(ENEMY_POLITICIAN_FLAG + conv);
            String flagStr = pw + EF;
            newFlag = Integer.parseInt(flagStr);
        }
        else if (flag == ENEMY_SLANDERER_NEARBY_FLAG){
            String EF = Integer.toString(ENEMY_SLANDERER_NEARBY_FLAG + conv);
            String flagStr = pw + EF;
            newFlag = Integer.parseInt(flagStr);
        }
        else if (flag == ENEMY_MUCKRAKER_NEARBY_FLAG){
            String EF = Integer.toString(ENEMY_MUCKRAKER_NEARBY_FLAG + conv);
            String flagStr = pw + EF;
            newFlag = Integer.parseInt(flagStr);
        }
        else if (flag == ENEMY_ENLIGHTENMENT_CENTER_FLAG) {
            String EF = Integer.toString(ENEMY_ENLIGHTENMENT_CENTER_FLAG + conv);
            String flagStr = pw + EF;
            newFlag = Integer.parseInt(flagStr);
        }
        System.out.println("New flag: " + newFlag);
        return newFlag;
    }

    /**
     * Supply a given robot's id, and this method will attempt to retrieve
     * the flag of the given bot and pass it to the helper parser method.
     *
     * A small hash map will be returned who's values can be searched for
     * using the FlagConstants as keys. Each flag/key will have an associated
     * MapLocation value.
     *
     * If there's an error at any point, then a map containing the key ERROR
     * will be returned.
     *
     * @param id
     * @return HashTable
     **/
    public HashMap<Integer, MapLocation> retrieveFlag (RobotController rc, int id) throws GameActionException {
        // hash table containing all flag info.
        // see FlagConstants.java for a breakdown on entries.
        HashMap<Integer, MapLocation> res = new HashMap<>();
        // try to get flag from a given bot
        if (rc.canSenseRobot(id)) {
            RobotInfo info = rc.senseRobot(id);
            int flag = rc.getFlag(info.getID());
            // make sure this is one of ours!
            if (isOurs(flag)) {
                System.out.println("retrieveFlag -> This was one of our flags!");
                res = parseFlag(info, flag);
            }
            else{
                // add our own location since the table requires a MapLocation
                System.out.println("retrieveFlag -> This wasn't one of our flags!");
                res.put(ERROR, rc.getLocation());
            }
        }
        else{
            System.out.println("retrieveFlag -> Could not sense bot from given ID!");
            res.put(ERROR, rc.getLocation());
        }
        return res;
    }

    public HashMap<Integer, MapLocation> parseFlag(RobotInfo info, int flagOrig){
        HashMap<Integer, MapLocation> res = new HashMap<>();
        // NOTE: this is redundant if parseFlag is called from retrieveFlag
        // this is here in case parseFlag is called separately.
        if (!isOurs(flagOrig)){
            System.out.println(("parseFlag -> Not one of our flags!"));
            res.put(ERROR, info.getLocation());
            return res;
        }
        int len = countDigis(flagOrig);
        System.out.println("parseFlag -> len of given flag: " + len);
        // this is an alert!
        if (len == 3){
            System.out.println("parseFlag -> Received an alert!");
            // remove first two digits, then test against constants
            int flag = flagOrig % 10;
            if (flag == NEUTRAL_ENLIGHTENMENT_CENTER_FLAG) {
                System.out.println("parseFlag -> Found a neutral EC!");
                res.put(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG, info.getLocation());
            }
            else if (flag == NEED_HELP) {
                System.out.println("parseFlag -> someone needs help!");
                res.put(NEED_HELP, info.getLocation());
            }
            else if (flag == GO_HERE) {
                System.out.println("parseFlag -> need to go this way!");
                res.put(GO_HERE, info.getLocation());
            }
            else {
                System.out.println("parseFlag -> Unable to parse 3-digit flag!");
                res.put(ERROR, info.getLocation());
            }
        }
        // this is enemy info!
        else if (len == 5){
            System.out.println("parseFlag -> Received enemy info!");
            // remove first two digits, then test against constants
            int flagTemp = flagOrig % 1000;
            // subtract last two digits to get base flag. i.e. 302 - 2 == 300
            int conv = flagTemp % 100;
            int flag = flagTemp - conv;
            if (flag == ENEMY_ENLIGHTENMENT_CENTER_FLAG) {
                System.out.println("parseFlag -> there's an enemy Enlightenment Center this way!");
                res.put(ENEMY_ENLIGHTENMENT_CENTER_FLAG, info.getLocation());
            }
            // enemy politician!
            if (flag/100 == 1) {
                System.out.println("parseFlag -> there's an enemy Politician this way!");
                res.put(ENEMY_POLITICIAN_FLAG, info.getLocation());
            }
            // enemy slanderer!
            else if (flag/100 == 2) {
                System.out.println("parseFlag -> there's an enemy Slanderer this way!");
                res.put(ENEMY_SLANDERER_NEARBY_FLAG, info.getLocation());
            }
            // enemy muckraker!
            else if (flag/100 == 3) {
                System.out.println("parseFlag -> there's an enemy Muckraker this way!");
                res.put(ENEMY_MUCKRAKER_NEARBY_FLAG, info.getLocation());
            }
            else {
                System.out.println("parseFlag -> unable to parse 5-digit flag!");
                res.put(ERROR, info.getLocation());
            }
        }
        // this is location info!
        else if (len == 8){
            System.out.println("parseFlag -> Received location info!");
            MapLocation loc = decodeLocationFromFlag(flagOrig);
            res.put(LOCATION_INFO, loc);
        }
        return res;
    }

    // Lil' helpers
    public Integer countDigis(int number){
        int count = 0;
        for(; number !=0; number/=10, ++count){}
        return count;
    }

    public Boolean isOurs(int flag){
        if (flag > 11300300) return false;
        int len = countDigis(flag);
        if (len == 7 || len == 6) return false;
        if (len == 4 || len < 3) return false;
        /*
         * PASSWORD CHECK!
         *
         * remove trailing digits by using n /= 10^(n-k), where n is total number of digits,
         * and k is the number of digits to remove. since n is unknown until we actually get a
         * flag, we'll have to count the total digits since our flags are either 3 or 5 digits long
         *
         */
        int firstTwo = flag / (int) Math.pow(10,(len-2));
        if (firstTwo != PASSWORD)
            return false;
        return true;
    }


    /**
     * Flag encoding methods:
     *
     * ENCODE:
     * 1. get x and y from MapLocation
     * 2. apply hash function (to be defined) to each coordinate
     * 3. change new x and y to strings
     * 4. concatinate
     * 5. change back to int and return.
     *
     * DECODE:
     * 1. Change 6-digit int to a string
     * 2. Split in two
     * 3. Change each half back into an int
     * 4. Put through reverse hash
     * 5. Create new MapLocation Object and assign
     *    to contained x and y fields.
     *
     */


    /**
     * Takes a MapLocation as an argument and returns encoded x/y coordinates
     * in the form of a setable flag. Returns an ERROR flag value if something
     * goes wrong.
     *
     * @param loc
     * @return
     */
    public int encodeLocationInFlag(MapLocation loc){
        String pw = Integer.toString(PASSWORD);
        String xS = Integer.toString(loc.x / 100);
        String yS = Integer.toString(loc.y / 100);
        String flagStr = pw + xS + yS;
        return Integer.parseInt(flagStr);
    }

    /**
     * Takes a given coordinates flag and returns a MapLocation object
     * containing the approximate x, y coordinates.
     *
     * @param flagOrig
     * @return MapLocation
     */
    public MapLocation decodeLocationFromFlag(int flagOrig){
        // is this flag less than 8 digits?s
        if(countDigis(flagOrig) < 8)
            return new MapLocation(0,0);
        // remove first two (since it's the password)
        int flagTemp = flagOrig % 1000000;
        // split into two separate integers
        String flagStr = Integer.toString(flagTemp);
        int mid = flagStr.length()/2;
        String xStr = flagStr.substring(0, mid);
        String yStr = flagStr.substring(mid);
        int x = Integer.parseInt(xStr);
        int y = Integer.parseInt(yStr);
        return new MapLocation(x *= 100, y *= 100);
    }

    /**
     * Find and return a list of all enemy bots within your surroundings
     * @return List
     * @throws NullPointerException
     */
    public HashMap<Integer, RobotInfo> findThreats(RobotController rc) throws NullPointerException{
        Team enemy = rc.getTeam().opponent();
        HashMap<Integer, RobotInfo> threats = null;
        for (RobotInfo robot : rc.senseNearbyRobots()) {
            if(robot.getTeam() == enemy){
                threats.put(ENEMY_INFO, robot);
            }
        }
        return threats;
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
