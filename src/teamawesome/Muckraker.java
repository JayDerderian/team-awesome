package teamawesome;
import battlecode.common.*;
import static teamawesome.FlagConstants.*;

public strictfp class Muckraker extends GenericRobot {

    /**
     * Variables of Muckraker
     */
    public String robotStatement = "I'm a " + rc.getType() + "! Location " + rc.getLocation();
    public boolean exposedSuccess = false;
    public MapLocation neutralLocation;
    public int flagSensed = 00000; // initial value = flag not set
    public MapLocation enemyECLocation;
    public MapLocation[] surroundingLocationArray;
    public Direction nextMoveDir;
    boolean nextMoveDirSet = false;
    public int xLean;
    public int yLean;
    public int dirIdx;
    public final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    /**
     * constructor
     * @param newRc - new RobotController Object
     */
    public Muckraker(RobotController newRc) {
        super(newRc);
    }

    /**
     * Muckraker logic
     * @throws GameActionException
     */
    public void turn() throws GameActionException {
        System.out.println(robotStatement);

        Team enemy = rc.getTeam().opponent();
        boolean DetectEnemySlanderer = false;
        Direction detectedDirection = Direction.CENTER; // random value, change later

        // 1. Sense Every Robot (max actionRadiusSquared)
        for (RobotInfo robot : rc.senseNearbyRobots()) {
            exposedSuccess = false;
            nextMoveDirSet = false;
            // ENEMY
            if(robot.getTeam() == enemy){
                if (robot.type.canBeExposed()) {
                    // It's a slanderer... go get them!
                    if (rc.canExpose(robot.location)) {
                        exposedSuccess = true;
                        System.out.println("e x p o s e d");
                        rc.expose(robot.location);
                        return;
                    }
                } else if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) { // Enemy enlightenment center
                    enemyECLocation = robot.getLocation();
                    determineNextMoveDir(enemyECLocation);
//                    nextMove(enemyECLocation);
                }
//                else {
//                    enemyECLocation = robot.getLocation();
////                    determineNextMoveDir(enemyECLocation);
//                    nextMove(enemyECLocation);
//                }
            }
            // NEUTRAL EC
            if(robot.getTeam() == Team.NEUTRAL) {
                neutralLocation = robot.getLocation();
                // generate flag and set flag
                if(rc.canSetFlag(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG))
                    rc.setFlag(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG);
            }
            // OUR TEAM ROBOT
            else if(robot.getTeam() != enemy) {
                if (rc.canGetFlag(robot.getID())){
                    // Get the Flag
                    flagSensed = rc.getFlag(robot.getID());
                    // Decode the Flag
                    // yet to implement...
                    // Flag about Neutral EC
                    if(flagSensed == NEUTRAL_ENLIGHTENMENT_CENTER_FLAG){
                        rc.setFlag(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG);
                    }
                    // Flag about Enemy Slanderer
                    else if(flagSensed == ENEMY_SLANDERER_NEARBY_FLAG){
                        DetectEnemySlanderer = true;
                        // decode their location
                        // set own flag value
                    }
                }
            }
        }

        // 2. Muckraker move --> toward Enemy slanderer (or) random direction
        if(nextMoveDirSet){
          tryMove(nextMoveDir);
        } else if(DetectEnemySlanderer){
             tryMove(detectedDirection);
        } else if (tryMove(randomDirection())){
            System.out.println("I moved!");
        }
    }

    private void determineNextMoveDir(MapLocation enemyLocation) throws GameActionException {
//        // North
//        MapLocation N = new MapLocation(enemyLocation.x, enemyLocation.y-1);
//        // East
//        MapLocation E = new MapLocation(enemyLocation.x+1, enemyLocation.y);
//        // South
//        MapLocation S = new MapLocation(enemyLocation.x, enemyLocation.y+1);
//        // West
//        MapLocation W = new MapLocation(enemyLocation.x-1, enemyLocation.y);
//        // NorthEast
//        MapLocation NE = new MapLocation(enemyLocation.x+1, enemyLocation.y-1);
//        // SouthEast
//        MapLocation SE = new MapLocation(enemyLocation.x+1, enemyLocation.y+1);
//        // SouthWest
//        MapLocation SW = new MapLocation(enemyLocation.x-1, enemyLocation.y+1);
//        // NorthWest
//        MapLocation NW = new MapLocation(enemyLocation.x-1, enemyLocation.y-1);
//
//        surroundingLocationArray = new MapLocation[]{N, E, S, W, NE, SE, SW, NW};
//        if (rc.canDetectLocation(N) && !rc.isLocationOccupied(N)) {
//
//        }

        MapLocation myLocation = rc.getLocation();
        int x = myLocation.x - enemyLocation.x;
        int y = myLocation.y - enemyLocation.y;
        if(x == 0) {
            if(y > 0)
                nextMoveDir = Direction.SOUTH;
            if(y < 0)
                nextMoveDir = Direction.NORTH;
        } else if(y == 0) {
            if(x > 0)
                nextMoveDir = Direction.WEST;
            if(x < 0)
                nextMoveDir = Direction.EAST;
        } else if(x > 0 && y > 0) {
            nextMoveDir = Direction.SOUTHWEST;
        } else if(x > 0 && y < 0) {
            nextMoveDir = Direction.NORTHWEST;
        } else if(x < 0 && y < 0) {
            nextMoveDir = Direction.NORTHEAST;
        } else if(x < 0 && y > 0) {
            nextMoveDir = Direction.SOUTHEAST;
        } else {
            nextMoveDir = randomDirection();
        }

    }
}
