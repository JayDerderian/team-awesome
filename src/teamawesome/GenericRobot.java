package teamawesome;
import static teamawesome.FlagConstants.*;

import battlecode.common.*;

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
     * @param rc
     * @param id
     * @throws GameActionException
     * @return HashTable
     **/
    protected Hashtable<Integer, Integer> getFlag(RobotController rc, int id) throws GameActionException {
        // hash table containing all flag info.
        // see FlagConstants.java for a breakdown on entries.
        Hashtable<Integer, Integer> res = new Hashtable<>();
        // try to get flag from a given bot
        if (rc.canGetFlag(id)) {
            // get flag from given bot (id)
            int flag = rc.getFlag(id);
            // convert flag to string for parsing purposes
            // System.out.print("getFlag() - I just got the flag: " + flag + "from ID: " + id);
            String flagStr = String.valueOf(flag);
            // System.out.print("getFlag() - flag string: " + flagStr);
            // is this one of our flags? get first two
            // characters and convert back to an int to test...
            // ... i know, it's silly.
            int firstTwo = Integer.parseInt(flagStr.substring(0,1));
            //System.out.print("The first two digits are:" + firstTwo);
            if (firstTwo != PASSWORD){
                // System.out.print("Not one of our flags!");
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
            //System.out.print("Could not get flag with a given ID!");
            res.put(ERROR, 0);
            return res;
        }
    }

    protected Hashtable<Integer, Integer> parseFlag (String flagStr){
        // results table
        Hashtable<Integer, Integer> results = new Hashtable<>();
        // This is an alert!
        if (flagStr.length() == 4){
            // System.out.print("I received an ALERT!");
            // remove first two digits (pw), then
            // determine alert type
            String alert = flagStr.substring(2,3);
            // NOTE make sure this returns 1 if the char is "1"!!!!!
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
            // actual (x,y) coordinates returned from rc.senseRobotAtLocation(MapLocation loc);

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
        // Something else!
        else{
            // System.out.print("Unable tp parse flag!");
            results.put(ERROR,0);
        }
        return results;
    }

    /**
     *
     * @param rc
     * @param flagType
     * @param id
     * @return Integer
     * @throws GameActionException
     *
     * Takes a RobotController instance (rc), a flagType (use FlagConstants as a parameter!)
     * and an ID of a robot needed to build a flag with. Use 0 for ID if a NEED_HELP flag is needed
     */

    protected Integer makeFlag(RobotController rc, int flagType, int id) throws GameActionException{
        // base flag
        int flag = 0;
        // Alert flag
        if(flagType == ALERT){
            // set password
            String pwStr = String.valueOf(PASSWORD);
            // Neutral EC spotted!
            if(id != 0) {
                if (rc.canSenseRobot(id)) {
                    RobotInfo info = rc.senseRobot(id);
                    MapLocation loc = info.location;
                    // Map dir flag???
                    // figure out  EC location in relation to current bots location

                    // Neutral EC!
                    if (info.type == RobotType.ENLIGHTENMENT_CENTER) {
                        String flagStr = pwStr + NEUTRAL_ENLIGHTENMENT_CENTER_FLAG + NORTH_WEST;
                        flag = Integer.parseInt(flagStr);
                    }
                }
                else{
                    // System.out.print("Unable to detect robot!");
                    return ERROR;
                }
            }
            // Need help!
            else{
                // get our current location
                MapLocation myLoc = rc.getLocation();
                // append to flag integer
            }
        }
        // Enemy info flag
        else if (flagType == ENEMY_INFO){
            // set password
            String pwStr = String.valueOf(PASSWORD);
            // gets info about specified enemy bot, then constructs flag
            if(rc.canSenseRobot(id)){
                // get sensed bot info
                RobotInfo info = rc.senseRobot(id);
                MapLocation loc = info.location;
                // Found a politician!
                if(info.type == RobotType.POLITICIAN){
                    // Map dir flag???
                    // figure out  EC location in relation to current bots location
                    int conv = info.getConviction();
                    int typeAndConv = ENEMY_POLITICIAN_FLAG + conv;
                    // NOTE NORTH is a placeholder until I can figure out how to get a bot
                    // directions to the location they need to go. TBD.
                    String flagStr = pwStr + typeAndConv + NORTH_WEST;
                    // finally, convert it all back to an int and set for current bot.
                    flag = Integer.parseInt(flagStr);
                    rc.setFlag(flag);
                }
                // Found a Slanderer!
                else if (info.type == RobotType.SLANDERER){
                    String typeStr = String.valueOf(ENEMY_SLANDERER_NEARBY_FLAG);
                    String flagStr = pwStr + typeStr + NORTH_WEST;
                    flag = Integer.parseInt(flagStr);
                    rc.setFlag(flag);
                }
                // Found a Muckraker!
                else if (info.type == RobotType.MUCKRAKER){
                    String typeStr = String.valueOf(ENEMY_MUCKRAKER_NEARBY_FLAG);
                    String flagStr = pwStr + typeStr + NORTH_WEST;
                    flag = Integer.parseInt(flagStr);
                    rc.setFlag(flag);
                }
                // Found an enemy Enlightenment Center!
                else if (info.type == RobotType.ENLIGHTENMENT_CENTER){
                    String typeStr = String.valueOf(ENEMY_ENLIGHTENMENT_CENTER_FLAG);
                    String flagStr = pwStr + typeStr + NORTH_WEST;
                    flag = Integer.parseInt(flagStr);
                    rc.setFlag(flag);

                }
            }
            else{
                // System.out.print("Unable to sense bot with given id!");
                return ERROR;
            }

        }
        if (flag != ERROR)
            return flag;
        else
            return ERROR;
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
