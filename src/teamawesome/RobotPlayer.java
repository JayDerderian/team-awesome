package teamawesome;

import static teamawesome.FlagConstants.*;
import battlecode.common.*;
import java.util.HashMap;
import java.lang.Math;
import java.util.LinkedList;
import java.util.Map;


/**
 * RobotPlayer (Abstract class)
 * This is the class that is called by the client environment to invoke a new robot
 * this class is static but its methods instantiate the robot objects
 *
 * This Class contains utility methods that every robot uses.
 */
abstract public strictfp class RobotPlayer {
    static RobotController rc;
    public boolean testMode;
    protected int rxsender;
    protected int rxsync;
    protected int rxcode;
    protected int rxtype;
    protected int txsync;
    protected int commlag;
    boolean hasSetFlag;
    LinkedList<Integer> rolodex;
    MapLocation scouted;
    int scoutedAge;
    protected static final int swizzle = 0;
    public int mothership = -1;
    public MapLocation motherLoc;

    public static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };
    public static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };
    static int turnCount;

    public RobotPlayer(RobotController newRc) {
        rc = newRc;
        // look around and learn who your mama is
        RobotInfo[] wheresMommy = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam());
        if(rc.getType() == RobotType.ENLIGHTENMENT_CENTER) {
            mothership = rc.getID();
            motherLoc = rc.getLocation();
        } else {
            for (RobotInfo info:
                    wheresMommy) {
                if(info.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    mothership = info.getID();
                    motherLoc = info.getLocation();
                }
            }
        }
        txsync = -1;
        rxsync = -1;
        rxtype = ERROR;
        rxsender = -1;
        rolodex = new LinkedList<>();
        scouted = null;
        scoutedAge = 0;
        commlag = 0;
        testMode = false;
        hasSetFlag = false;
    }

    /**
     * Abstract method for actuating the robot for one turn
     * Must be implemented by child class
     * @throws GameActionException
     */
    abstract void turn() throws GameActionException;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot, and to get information on its current status.
        RobotPlayer.rc = rc;
        Politician politic = new Politician(rc);
        turnCount = 0;
        // The reference variable is used to refer to the objects of derived classes (subclasses of abstract class)
        RobotPlayer robot; // the identity for this robot
        // initialize the robot identity according to the type stored in rc
        switch (rc.getType()) {
            case ENLIGHTENMENT_CENTER: robot = new EnlightenmentCenter(rc); break;
            case POLITICIAN:           robot = new Politician(rc);          break;
            case SLANDERER:            robot = new Slanderer(rc);           break;
            case MUCKRAKER:            robot = new Muckraker(rc);           break;
            default:
                throw new IllegalStateException("Unexpected value: " + rc.getType());
        }

        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            turnCount += 1;
            // special case: slanderers become politicians after some time
            if(robot.getClass() == Slanderer.class && rc.getType() == RobotType.POLITICIAN) {
                int mother = robot.mothership;
                MapLocation loc = robot.motherLoc;
                robot = new Politician(RobotPlayer.rc); // remake this robot as a politician
                robot.mothership = mother;
                robot.motherLoc = loc;
            }
            try {
                // actuate the robot for one round
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                robot.turn();

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }
    /**
     * Function to sync reception of location data
     * @param ID robot to receive location data from
     * @return the location of the robot
     * @throws GameActionException
     */
    protected Map<Integer, MapLocation> rxLocation(int ID) throws GameActionException{
        System.out.println("Reception initiated");
        if(rxsender == -1) rxsender = ID;
        if(rxsender != ID) return null; // only talk to current rxsender
        Map<Integer, MapLocation> flag = retrieveFlag(rc, ID);
        Map.Entry<Integer, MapLocation> flagval = flag.entrySet().iterator().next();
        int code = flagval.getKey();
        MapLocation loc = flagval.getValue();
        if(rxsync == -1) {
            // if rxsync is unset and the flag is valid, read the flag and set rxsync
            if(code != ERROR && code != LOCATION_INFO)
                rxsync = 1;
            rxcode = code;
            flag.clear();
            flag.put(code, null);
            commlag = 0;
        } else if(rxsync == 1) {
            // if rxsync is a valid value, clear flag and put the stored code
            flag.clear();
            try {
                MapLocation decipherLoc = loc.translate(swizzle, swizzle);
                rc.setIndicatorDot(decipherLoc, 255, 0, 255);
                System.out.println("rx: " + ID + " reports code " +
                        rxcode + " at " + decipherLoc);
                if(validateLocation(decipherLoc) || testMode) {
                    flag.put(rxcode, decipherLoc);
                    System.out.println("Receive success!");
                } else {
                    System.out.println("Invalid location received, terminating communication");
                    rxsender = -1;
                }
            } catch(NullPointerException n) {
                System.out.println("No Location data found!");
                rxsender = -1;
            }
            rxsync = -1;
        } else if(commlag < 5) {
            System.out.println("Lagging... waiting " + (5 - commlag) + " more rounds");
            ++commlag;
        } else {
            System.out.println("Communication error, terminating communication");
            rxsender = -1;
            rxsync = -1;
        }
        return flag;
    }



    protected void updateContact(RobotInfo robot) throws GameActionException {
        if(robot.getType() != RobotType.MUCKRAKER && robot.getType() != RobotType.SLANDERER) return;
        int friendID = robot.getID();
        System.out.println("Found a friend! ID #" + friendID);
        if(!rolodex.contains(friendID)) rolodex.add(friendID);
    }

    /**
     * Function to go through rolodex and check each stored robot
     * @throws GameActionException cause RobotController
     */
    protected void checkRolodex() throws GameActionException{
        System.out.println("I have " + rolodex.size() + " contacts. Here is my rolodex:");
        LinkedList<Integer> toRemove = new LinkedList<>();
        for (Integer id:
                rolodex) {
            if(Clock.getBytecodesLeft() < 500 && !testMode) return;
            try {
                if(rc.canGetFlag(id)){
                    RobotInfo info;
                    Map<Integer, MapLocation> flag;
                    if(!rc.canSenseRobot(id))
                        flag = retrieveFlag(rc, id);
                    else {
                        info = rc.senseRobot(id);
                        flag = retrieveFlag(rc, info);
                    }
                    if(flag.containsKey(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG)) {
                        // if no rx source, use this one
                        System.out.println("ID #" + id + " found a neutral EC! Beginning comms");
                        if (rxsender == -1) rxsender = id;
                        break;
                    } else if(flag.containsKey(ENEMY_ENLIGHTENMENT_CENTER_FLAG)) {
                        // if no rx source, use this one
                        System.out.println("ID #" + id + " found an enemy EC! Beginning comms");
                        if (rxsender == -1) rxsender = id;
                        break;
                    } else {
                        System.out.println("Hi ID #" + id + ", glad you're still out there!");
                    }
                } else {
                    System.out.println("ID#" + id + " is out of comms range!");
                }
            } catch(GameActionException e) { // the ID could not be found, meaning it's time to delete that entry
                System.out.println("ID #" + id + " is dead!");
                if(rxsender == id) rxsender = -1;
                toRemove.add(id);
            }
        }
        for (Integer del:
                toRemove) {
            int startcode = Clock.getBytecodeNum();
            rolodex.remove(del);
            System.out.println("checkRolodex: removing entry used " + (Clock.getBytecodeNum() - startcode) + " bytecodes");

        }
    }

    /**
     * Function to sync the transmission of location data via the flag system
     * @param type the message to be sent
     * @param loc the location to be encoded
     * @param conv conviction level
     * @return success of the operation
     * @throws GameActionException because rc
     */
    protected boolean txLocation(int type, MapLocation loc, int conv) throws GameActionException{
        //if(loc == null) return false;
        System.out.println("Transmission Initiated, sending code " + type + " for location " + loc);
        if(txsync != -1) {
            if(loc == null) {
                System.out.println("comms error!");
            } else {
                // we broadcast the type last time, now broadcast location
                txsync = -1;
                MapLocation cipherLoc = loc.translate(-swizzle, -swizzle);
                int newFlag = encodeLocationInFlag(cipherLoc);
                if(rc.canSetFlag(newFlag)) {
                    rc.setFlag(newFlag);
                    hasSetFlag = true;
                    return true;
                }
            }
        } else {
            // we need to broadcast the type
            txsync = 1;
            int newFlag = makeFlag(type, conv);
            if(rc.canSetFlag(newFlag)) {
                rc.setFlag(newFlag);
                hasSetFlag = true;
                return true;
            }
        }
        return false;
    }

    /**
     * given a location, validates that it is a possible location given BattleCode rules
     * @param loc location to be validated
     * @return validity of loc
     */
    protected boolean validateLocation(MapLocation loc) {
        if(loc == null) return false;
        int x = loc.x;
        int y = loc.y;
        if(x < 10000 || x > 30000) return false;
        return y >= 10000 && y <= 30000;
    }


    /**
     * Takes a the base flag (see FlagConstants.java), and optional conviction level.
     * Returns a new flag.
     *
     * Enter 0 for conv if none is detected, or if you're setting an ALERT.
     *
     * @return Integer
     */
    public int makeFlag(int flag, int conv) {
        int newFlag = 0;
        String pw = Integer.toString(PASSWORD);
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
        else if (flag == SEND_LOCATION){
            String EF = Integer.toString(SEND_LOCATION + conv);
            String flagStr = pw + EF;
            newFlag = Integer.parseInt(flagStr);
        }
        else if (flag == NEUTRAL){
            String EF = Integer.toString(NEUTRAL + conv);
            String flagStr = pw + EF;
            newFlag = Integer.parseInt(flagStr);
        }
        else if (flag == LOCATION_INFO){
            String EF = Integer.toString(LOCATION_INFO + conv);
            String flagStr = pw + EF;
            newFlag = Integer.parseInt(flagStr);
        }
        else if (flag == OUT_OF_RANGE){
            String EF = Integer.toString(OUT_OF_RANGE + conv);
            String flagStr = pw + EF;
            newFlag = Integer.parseInt(flagStr);
        }
        else if (flag == ENEMY_INFO){
            String EF = Integer.toString(ENEMY_INFO + conv);
            String flagStr = pw + EF;
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
        return newFlag;
    }


    /**
     * Supply a RobotController instance (rc), and a robot's id, and this method
     * will attempt to retrieve the flag of the given bot and pass it to the helper
     * parser method.
     *
     * A small hash map will be returned who's values can be searched for
     * using the FlagConstants as keys. Each flag/key will have an associated
     * MapLocation value.
     *
     * If there's an error at any point, then a map containing the key ERROR
     * will be returned.
     *
     * @return HashTable
     **/
    public HashMap<Integer, MapLocation> retrieveFlag (RobotController rc, RobotInfo info) throws GameActionException {
        // hash table containing all flag info.
        // see FlagConstants.java for a breakdown on entries.
        HashMap<Integer, MapLocation> res = new HashMap<>();
        // try to get flag from a given bot
        int id = info.getID();
        return retrieveFlag(rc, info, res, id);
    }

    public HashMap<Integer, MapLocation> retrieveFlag (RobotController rc, int ID) throws GameActionException {
        // hash table containing all flag info.
        // see FlagConstants.java for a breakdown on entries.
        HashMap<Integer, MapLocation> res = new HashMap<>();
        // try to get flag from a given bot;
        return retrieveFlag(rc, null, res, ID);
    }

    private HashMap<Integer, MapLocation> retrieveFlag(RobotController rc, RobotInfo info, HashMap<Integer, MapLocation> res, int id) throws GameActionException {
        if (rc.canSenseRobot(id)) {
            int flag = rc.getFlag(id);
            // make sure this is one of ours!
            if (isOurs(flag))
                res = parseFlag(info, flag);
            else
                // add our own location since the table requires a MapLocation
                res.put(ERROR, rc.getLocation());
        }
        else if(this.getClass() == EnlightenmentCenter.class && rc.canGetFlag(id)) {
            int flag = rc.getFlag(id);
            res = parseFlag(null, flag);
        }
        else
            res.put(ERROR, rc.getLocation());
        return res;
    }

    public HashMap<Integer, MapLocation> parseFlag(RobotInfo info, int flagOrig){
        HashMap<Integer, MapLocation> res = new HashMap<>();
        // NOTE: this is redundant if parseFlag is called from retrieveFlag
        // this is here in case parseFlag is called separately.
        MapLocation location;
        if(info == null) location = null;
        else location = info.getLocation();
        if (!isOurs(flagOrig)){
            System.out.println("Not one of ours! proceed with caution");
            res.put(ERROR, location);
            return res;
        }
        int len = countDigis(flagOrig);
        System.out.println("Flag length: " + len);
        // this is an alert!
        if (len == 3){
            // remove first two digits, then test against constants
            int flag = flagOrig % 10;
            if (flag == NEUTRAL_ENLIGHTENMENT_CENTER_FLAG)
                res.put(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG, location);
            else if (flag == NEED_HELP)
                res.put(NEED_HELP, location);
            else if (flag == GO_HERE)
                res.put(GO_HERE, location);
            else {
                System.out.println("parseFlag -> Unable to parse 3-digit flag!");
                res.put(ERROR, location);
            }
        }
        // this is enemy info!
        else if (len == 5){
            // remove first two digits, then test against constants
            int flagTemp = flagOrig % 1000;
            // subtract last two digits to get base flag. i.e. 302 - 2 == 300
            int conv = flagTemp % 100;
            int flag = flagTemp - conv;
            if (flag == ENEMY_ENLIGHTENMENT_CENTER_FLAG)
                res.put(ENEMY_ENLIGHTENMENT_CENTER_FLAG, location);
            // enemy politician!
            if (flag/100 == 1)
                res.put(ENEMY_POLITICIAN_FLAG, location);
                // enemy slanderer!
            else if (flag/100 == 2)
                res.put(ENEMY_SLANDERER_NEARBY_FLAG, location);
                // enemy muckraker!
            else if (flag/100 == 3)
                res.put(ENEMY_MUCKRAKER_NEARBY_FLAG, location);
            else
                res.put(ERROR, location);
        }
        // this is location info!
        else if (len == 8){
            System.out.println("Location data received: " + flagOrig + " Attempting to decode");
            MapLocation loc = decodeLocationFromFlag(flagOrig);
            res.put(LOCATION_INFO, loc);
        }
        return res;
    }

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
        // subtract the mothership location, add 64 to avoid negative values
        String xS = String.format("%03d", loc.x - motherLoc.x + 64);
        String yS = String.format("%03d", loc.y - motherLoc.y + 64);
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
        String flagStr = Integer.toString(flagOrig);
        String xStr = flagStr.substring(2, 5);
        String yStr = flagStr.substring(5);
        int x = Integer.parseInt(xStr);
        int y = Integer.parseInt(yStr);
        return new MapLocation(x + motherLoc.x - 64, y + motherLoc.y - 64);
    }

    /**
     * Find and return a list of all enemy bots within your surroundings
     *
     * @param rc
     * @return HashMap
     */
    public HashMap<Integer, MapLocation> findThreats(RobotController rc){
        Team enemy = rc.getTeam().opponent();
        HashMap<Integer, MapLocation> threats = new HashMap<>();
        for (RobotInfo robot : rc.senseNearbyRobots()) {
            if(robot.getTeam() == enemy){
                threats.put(ENEMY_INFO, robot.getLocation());
            }
        }
        if(threats.isEmpty())
            threats.put(ERROR, new MapLocation(0, 0));
        return threats;
    }

    /**
     * Attempts to detect and store location info of a neutral EC
     * within the vicinity of this bot
     *
     * @param rc
     * @return HashMap
     */
    public HashMap<Integer, MapLocation> findNeutralECs (RobotController rc) {
        HashMap<Integer, MapLocation> ecLoc = new HashMap<>();
        for (RobotInfo robot : rc.senseNearbyRobots()) {
            if(robot.getTeam() == Team.NEUTRAL ){
                ecLoc.put(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG, robot.getLocation());
            }
        }
        if(ecLoc.isEmpty())
            ecLoc.put(ERROR, new MapLocation(0,0));
        return ecLoc;
    }

    /**
     * Borrowed from examplefuncsplayer, Returns a random Direction.
     * @return a random Direction
     */
    protected Direction randomDirection() {
        return RobotPlayer.directions[(int) (Math.random() * RobotPlayer.directions.length)];
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
