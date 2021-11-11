package teamawesome;
import battlecode.common.*;

import java.util.*;

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
    public Direction enemyECDirection;
    public MapLocation[] surroundingLocationArray;
    public Direction nextMoveDir;
    boolean nextMoveDirSet = false;
    HashMap<Direction, Integer> mapDirectionNum = new HashMap<>();
    MapLocation homeECLocation;
    ArrayList<Direction> zigZagUp = new ArrayList<Direction>(Arrays.asList(Direction.EAST, Direction.NORTH, Direction.WEST, Direction.NORTH));
    ArrayList<Direction> zigZagDown = new ArrayList<Direction>(Arrays.asList(Direction.SOUTH, Direction.WEST, Direction.NORTH));
    int zigZagIndex = 0;
    boolean enemyEcFound = false;


    /**
     * constructor
     * @param newRc - new RobotController Object
     */
    public Muckraker(RobotController newRc) {
        super(newRc);
//        mapDirectionNum.put(Direction.NORTH, 0);
//        mapDirectionNum.put(Direction.EAST, 0);
//        mapDirectionNum.put(Direction.SOUTH, 0);
//        mapDirectionNum.put(Direction.WEST, 0);
//        mapDirectionNum.put(Direction.CENTER, 0); // map.get(key)
//        mapDirectionNum.put(Direction.NORTHEAST, 0);
//        mapDirectionNum.put(Direction.NORTHWEST, 0);
//        mapDirectionNum.put(Direction.SOUTHEAST, 0);
//        mapDirectionNum.put(Direction.SOUTHWEST, 0);
    }

    /**
     * Muckraker logic
     * @throws GameActionException
     */
    public void turn() throws GameActionException {
        System.out.println(robotStatement);
        homeECLocation = rc.getLocation();

        Team enemy = rc.getTeam().opponent();
        enemyEcFound = false;

        RobotInfo[] sensedNearByRobots =  rc.senseNearbyRobots();

        while(!enemyEcFound){
            for (RobotInfo robot : sensedNearByRobots) {
                // ENEMY
                if(robot.getTeam() == enemy) {
                    if (robot.type.canBeExposed()) {
                        // It's a slanderer... go get them!
                        if (rc.canExpose(robot.location)) {
                            exposedSuccess = true;
                            System.out.println("e x p o s e d");
                            rc.expose(robot.location);
                            return;
                        }
                    }
                    if(robot.type == RobotType.ENLIGHTENMENT_CENTER){
                        enemyEcFound = true;
                        enemyECLocation = robot.location;
                        enemyECDirection = robot.location.directionTo(enemyECLocation);
                        break;
                    }
                }
            }

            if(enemyEcFound)
                break;

            int stepCount = 1;
            while (tryMove(zigZagUp.get(zigZagIndex))){
                System.out.println("I moved!");
                stepCount += 1;
                if(stepCount == 30)
                    break;
            }
            sensedNearByRobots =  rc.senseNearbyRobots();
            if(!rc.onTheMap(rc.adjacentLocation(zigZagUp.get(zigZagIndex + 1 % zigZagUp.size()))))
                zigZagIndex = zigZagIndex+1 % zigZagUp.size();
        }
        if(enemyEcFound){
            // move to its adjacent location
            while (rc.adjacentLocation(enemyECDirection) != enemyECLocation) {
                if (tryMove(enemyECDirection)) {
                    System.out.println("I moved!");
                } else if (tryMove(randomDirection())) {
                    System.out.println("I moved!");
                }
            }

            if(rc.adjacentLocation(enemyECDirection) == enemyECLocation){
                sensedNearByRobots =  rc.senseNearbyRobots();
                for (RobotInfo robot : sensedNearByRobots) {
                    // ENEMY
                    if(robot.getTeam() == enemy) {
                        if (robot.type.canBeExposed()) {
                            // It's a slanderer... go get them!
                            if (rc.canExpose(robot.location)) {
                                exposedSuccess = true;
                                System.out.println("e x p o s e d");
                                rc.expose(robot.location);
                                return;
                            }
                        }
                    }
                }
            }

        }
    }


//    public void turn() throws GameActionException {
//        homeECLocation = rc.getLocation();
//
//        System.out.println(robotStatement);
//
//        Team enemy = rc.getTeam().opponent();
//        boolean DetectEnemySlanderer = false;
//        Direction detectedDirection = Direction.CENTER; // random value, change later
//        RobotInfo[] sensedNearByRobots =  rc.senseNearbyRobots();
//
//        // 1. Sense Every Robot (max actionRadiusSquared)
//        for (RobotInfo robot : sensedNearByRobots) {
//            exposedSuccess = false;
////            nextMoveDirSet = false;
//            // ENEMY
//            if(robot.getTeam() == enemy){
//                if (robot.type.canBeExposed()) {
//                    // It's a slanderer... go get them!
//                    Direction enemySlandererDir = robot.location.directionTo(robot.location);
//                    mapDirectionNum.computeIfPresent(enemySlandererDir, (k, v) -> v + 1);
//                    if (rc.canExpose(robot.location)) {
//                        exposedSuccess = true;
//                        System.out.println("e x p o s e d");
//                        rc.expose(robot.location);
//                        return;
//                    }
//                } else if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) { // Enemy enlightenment center
//                    Direction enemyECDir = robot.location.directionTo(robot.getLocation());
//                    mapDirectionNum.computeIfPresent(enemyECDir, (k, v) -> v + 2);
//                }
//            }
//            // NEUTRAL EC
//            if(robot.getTeam() == Team.NEUTRAL) {
//                neutralLocation = robot.getLocation();
//                // generate flag and set flag
//                if(rc.canSetFlag(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG))
//                    rc.setFlag(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG);
//            }
//            // OUR TEAM ROBOT
//            else if(robot.getTeam() != enemy) {
//                if (rc.canGetFlag(robot.getID())){
//                    // Get the Flag
//                    flagSensed = rc.getFlag(robot.getID());
//                    if(robot.getType() == RobotType.ENLIGHTENMENT_CENTER){
//                        // decode what our EC is saying.
//                    }
//                    // Decode the Flag
//                    // yet to implement...
//                    // Flag about Neutral EC
//                    // Flag about Enemy Slanderer
//                    if(flagSensed == ENEMY_SLANDERER_NEARBY_FLAG){
//                        DetectEnemySlanderer = true;
//                        // decode their location
//                        // set own flag value
//                    }
//                }
//            }
//        }
//
////        for (int i = 0; i < zigZagUp.size(); i=i%zigZagUp.size()-1){
////            while (tryMove(zigZagUp.get(i))){
////            System.out.println("I moved!");
////            if(sensedNearByRobots)
////            }
////        }
//
//    }

//    private void resetMapDirNum() {
//        mapDirectionNum.replaceAll((k,v) -> 0);
//    }

//    private Direction getPopularMapDirection() {
//        int max = Collections.max(mapDirectionNum.values());
//        if(max !=0){
//            for (Map.Entry<Direction, Integer> entry : mapDirectionNum.entrySet()) {
//                if (entry.getValue()==max) {
//                    return entry.getKey();
//                }
//            }
//        }
//        return randomDirection();
//    }

}
