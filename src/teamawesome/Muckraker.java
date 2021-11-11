package teamawesome;
import battlecode.common.*;

import java.util.*;

import static teamawesome.FlagConstants.*;

public strictfp class Muckraker extends GenericRobot {

    /**
     * Variables of Muckraker
     */
    public String robotStatement = "I'm a " + rc.getType() + "! Location " + rc.getLocation();

    public MapLocation enemyECLocation;
    public Direction enemyECDirection;

    HashMap<Direction, Integer> mapDirectionNum = new HashMap<>();

    boolean enemyEcFound = false;
    boolean hasPrevMovedDir = false;
    Direction prevMovedDir;
    boolean botDirectionToMoveSet = false;
    Direction botDirectionToMove;
    public int xLean;
    public int yLean;
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
    public int dirIdx;

    /**
     * constructor
     * @param newRc - new RobotController Object
     */
    public Muckraker(RobotController newRc) {
        super(newRc);
        mapDirectionNum.put(Direction.CENTER, 0);
        mapDirectionNum.put(Direction.NORTH, 1);
        mapDirectionNum.put(Direction.EAST, 2);
        mapDirectionNum.put(Direction.SOUTH, 3);
        mapDirectionNum.put(Direction.WEST, 4);
        mapDirectionNum.put(Direction.NORTHEAST, 5);
        mapDirectionNum.put(Direction.SOUTHEAST, 6);
        mapDirectionNum.put(Direction.SOUTHWEST, 7);
        mapDirectionNum.put(Direction.NORTHWEST, 8);
    }

    /**
     * Muckraker logic
     * @throws GameActionException
     */
    public void turn() throws GameActionException {
        xLean = 0; yLean = 0; // Reset guiding
        System.out.println(robotStatement);
//        homeECLocation = rc.getLocation();

        Team enemy = rc.getTeam().opponent();
        enemyEcFound = false;

        RobotInfo[] sensedNearByRobots = rc.senseNearbyRobots();

        for (RobotInfo robot : sensedNearByRobots) {
            // ENEMY
            if (robot.getTeam() == enemy) {
                if (robot.type.canBeExposed()) {
                    // It's a slanderer... go get them!
                    if (rc.canExpose(robot.location)) {
//                        exposedSuccess = true;
                        System.out.println("e x p o s e d");
                        rc.expose(robot.location);
                        return;
                    }
                }
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                    enemyEcFound = true;
                    enemyECLocation = robot.location;
                    enemyECDirection = robot.location.directionTo(enemyECLocation);
                    // set Flag to let other muck's know
                    int flagValue = makeFlag(ENEMY_ENLIGHTENMENT_CENTER_FLAG, mapDirectionNum.get(enemyECDirection));
                    if (rc.canSetFlag(flagValue))
                        rc.setFlag(flagValue);
                    botDirectionToMove = enemyECDirection;
                    botDirectionToMoveSet = true;
                    break;
                }
            } else if (robot.getTeam() != enemy){
                if(rc.canGetFlag(robot.ID)){
                    int flagValue = rc.getFlag(robot.ID);

                    if(robot.getType() == RobotType.MUCKRAKER){
                        if(flagValue-(flagValue%10) == 114 && !botDirectionToMoveSet) {
                            for (Map.Entry<Direction, Integer> mapSet :  mapDirectionNum.entrySet()) {
                                if (mapSet.getValue() == flagValue%10) {
                                    botDirectionToMove = mapSet.getKey();
                                    botDirectionToMoveSet = true;
                                }
                            }
                        }
                    } else {
                        if(flagValue == 1140 && !botDirectionToMoveSet) {
                            botDirectionToMove = robot.location.directionTo(robot.getLocation());
                            botDirectionToMoveSet = true;
                        }
                    }
                }
            }
        }

        // Move
//        if(botDirectionToMoveSet){
//            if(tryMove(botDirectionToMove)) {
//                System.out.println("Muck Moved!");
//                hasPrevMovedDir = true;
//                prevMovedDir = botDirectionToMove;
//            } else {
//                Direction nextRandom = randomDirection();
//                if (tryMove(nextRandom)) {
//                    System.out.println("Muck moved!");
//                    hasPrevMovedDir = true;
//                    prevMovedDir = nextRandom;
//                }
//            }
//        } else {
//            Direction leastPassabilityDirection = getLeastPassableDirection();
//            if(tryMove(leastPassabilityDirection)) {
//                hasPrevMovedDir = true;
//                prevMovedDir = leastPassabilityDirection;
//                System.out.println("Muck Moved!");
//            } else {
//                Direction nextRandom = randomDirection();
//                if (tryMove(nextRandom)) {
//                    System.out.println("Muck moved!");
//                    hasPrevMovedDir = true;
//                    prevMovedDir = nextRandom;
//                }
//            }
//        }

        if (xLean == 0 && yLean == 0) {
            int[] x = {0, 1, -1, 3, -3, 2, -2, 4, -4};
            for (int i: x) {
                if (rc.canMove(directions[myMod((dirIdx + i), directions.length)])) {
                    rc.move(directions[myMod((dirIdx + i), directions.length)]);
                    dirIdx += i;
                    break;
                }
            }
        }
        else {
            // Clean the leans somewhat
            if (Math.abs(xLean) > 2 * Math.abs(yLean)) {yLean = 0;}
            else if (Math.abs(yLean) > 2 * Math.abs(xLean)) {xLean = 0;}
            xLean = Math.min(1, Math.max(-1, xLean)) * -1;
            yLean = Math.min(1, Math.max(-1, yLean)) * -1;
            for (Direction dir : directions) {
                if (dir.getDeltaY() == yLean && dir.getDeltaX() == xLean) {
                    System.out.println("I'm moving to " + dir);
                    if (rc.canMove(dir)) { rc.move(dir); }
                    return;
                }
            }
            System.out.println("Cannot Move!!!");
        }

        botDirectionToMoveSet = false;
//        if (rc.canSetFlag(1000))
//            rc.setFlag(1000);
    }

    private Direction getLeastPassableDirection() throws GameActionException {
        double minPass = 1.0;
        Direction minPassDir = randomDirection();
        for(Direction d: Direction.values()){
            if(hasPrevMovedDir && prevMovedDir.opposite() != d){
                double currPass = rc.sensePassability(rc.adjacentLocation(d));
                if(currPass < minPass){
                    minPass = currPass;
                    minPassDir = d;
                }
            }
        }
        return minPassDir;
    }

    public int myMod(int i, int j) {
        return (((i % j) + j) % j);
    }
}
