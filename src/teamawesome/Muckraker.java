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
    boolean hasPrevMovedDir = false;
    Direction prevMovedDir;
    boolean botDirectionToMoveSet = false;
    Direction botDirectionToMove;

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
        homeECLocation = rc.getLocation();

        Team enemy = rc.getTeam().opponent();
        enemyEcFound = false;

        RobotInfo[] sensedNearByRobots = rc.senseNearbyRobots();

        for (RobotInfo robot : sensedNearByRobots) {
            // ENEMY
            if (robot.getTeam() == enemy) {
                if (robot.type.canBeExposed()) {
                    // It's a slanderer... go get them!
                    if (rc.canExpose(robot.location)) {
                        exposedSuccess = true;
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
                    int flagValue = makeFlag(ENEMY_ENLIGHTENMENT_CENTER_FLAG, 0);
                    if (rc.canSetFlag(flagValue))
                        rc.setFlag(flagValue);
                    botDirectionToMove = enemyECDirection;
                    botDirectionToMoveSet = true;
                    break;
                }
            } else if (robot.getTeam() != enemy){
                if(rc.canGetFlag(robot.ID)){
                    int flagValue = rc.getFlag(robot.ID);
                    if(flagValue == 1140 && !botDirectionToMoveSet){
                        botDirectionToMove = robot.location.directionTo(robot.getLocation());
                        botDirectionToMoveSet = true;
                    }
                }
            }
        }

        // Move
        if(botDirectionToMoveSet){
            if(tryMove(botDirectionToMove)) {
                System.out.println("Muck Moved!");
                hasPrevMovedDir = true;
                prevMovedDir = botDirectionToMove;
            } else {
                Direction nextRandom = randomDirection();
                if (tryMove(nextRandom)) {
                    System.out.println("Muck moved!");
                    hasPrevMovedDir = true;
                    prevMovedDir = nextRandom;
                }
            }
        } else {
            Direction leastPassabilityDirection = getLeastPassableDirection();
            if(tryMove(leastPassabilityDirection)) {
                hasPrevMovedDir = true;
                prevMovedDir = leastPassabilityDirection;
                System.out.println("Muck Moved!");
            } else {
                Direction nextRandom = randomDirection();
                if (tryMove(nextRandom)) {
                    System.out.println("Muck moved!");
                    hasPrevMovedDir = true;
                    prevMovedDir = nextRandom;
                }
            }
        }

        botDirectionToMoveSet = false;

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
}
