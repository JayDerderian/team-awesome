package polyMuckMob;

import battlecode.common.*;

import java.util.Random;

import static teamawesome.FlagConstants.ENEMY_ENLIGHTENMENT_CENTER_FLAG;
import static teamawesome.FlagConstants.ENEMY_MUCKRAKER_NEARBY_FLAG;

public class RobotPlayer {
    static RobotController rc;

    static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };

    static final Direction[] directions = {
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
    protected static final int SLAN_RUSH = 300;
//    private static boolean enemyEcFound;
    public static MapLocation enemyECLocation;
    public static Direction enemyECDirection;
    public static Direction botDirectionToMove;
    public static Direction prevMovedDir;
    public static int xLean;
    public static int yLean;
    public static int dirIdx;
    public static boolean enemyECLocationSet = false;
    public static boolean enemyEcFound = false;
    public static boolean neutralECLocationSet = false;
    public static boolean neutralEcFound = false;
    public static MapLocation neutralECLocation;
    public static Direction neutralECDirection;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        polyMuckMob.RobotPlayer.rc = rc;
        turnCount = 0;

        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER:
                        teamawesome.EnlightenmentCenter.run(rc);
                        break;
                    case POLITICIAN:
                        runPolitician();
                        break;
                    case SLANDERER:
                        teamawesome.Slanderer.run(rc);
                        break;
                    case MUCKRAKER:
                        teamawesome.Muckraker.run(rc);
                        break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runEnlightenmentCenter() throws GameActionException {
        RobotType toBuild;
        // build only slanderers for the first SLAN_RUSH rounds
        if(rc.getRoundNum() < SLAN_RUSH) {
            toBuild = RobotType.MUCKRAKER;
        } else {
            toBuild = randomSpawnableRobotType();
        }
        int influence = 50;
        for (Direction dir : directions) {
            if (rc.canBuildRobot(toBuild, dir, influence)) {
                rc.buildRobot(toBuild, dir, influence);
            } else {
                break;
            }
        }
    }

    static void runPolitician() throws GameActionException {
        xLean = 0; yLean = 0; // Reset guiding
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);

        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            System.out.println("empowering...");
            rc.empower(actionRadius);
            System.out.println("empowered");
            return;
        } else {
            RobotInfo[] convertable = rc.senseNearbyRobots(actionRadius, Team.NEUTRAL);
            if(convertable.length != 0 && rc.canEmpower(actionRadius)) {
                System.out.println("you will be assimilated");
                rc.empower(actionRadius);
            }
        }
        for (RobotInfo robot : rc.senseNearbyRobots()) {
            // ENEMY
            if(robot.getTeam() == Team.NEUTRAL) {
                neutralECFoundAction(robot, 1);
                break;
            } else if (robot.getTeam() != enemy) { // OUR TEAM
                if(rc.canGetFlag(robot.ID)) {
                    int flagValue = rc.getFlag(robot.ID);
                    if(flagValue == 111) {
                        botDirectionToMove = rc.getLocation().directionTo(robot.getLocation());
                        while(!neutralEcFound) {
                            if (tryMove(botDirectionToMove)) {
                                prevMovedDir = botDirectionToMove;
                                System.out.println("poly moved!");
                            }
                            else
                                break;
                        }
                        for (RobotInfo robot1 : rc.senseNearbyRobots()) {
                            if(robot1.getTeam() == Team.NEUTRAL) {
                                neutralECFoundAction(robot, 1);
                                break;
                            }
                        }
                    }
                } }
        }

//        if (tryMove(randomDirection()))
//            System.out.println("I moved!");
        if(!neutralEcFound) { // Initially explore map quickly (along with Slanders)
            if (xLean == 0 && yLean == 0) {
                int[] x1 = {0, 1, -1, 3, -3, 2, -2, 4, -4};
//                int randomIndex = new Random().nextInt(x1.length);
//                int arrLength = x1.length - 1;
//                int i;
                for (int i : x1) {
//                    i = x1[randomIndex];
                    if (rc.canMove(teamawesome.RobotPlayer.directions[myMod((dirIdx + i), teamawesome.RobotPlayer.directions.length)])) {
                        rc.move(teamawesome.RobotPlayer.directions[myMod((dirIdx + i), teamawesome.RobotPlayer.directions.length)]);
                        dirIdx += i;
                        break;
                    }
                    if (enemyEcFound)
                        break;
//                    arrLength--;
//                    randomIndex = (randomIndex + 1) % x1.length;
                }
            } }else { // If enemy EC found, then move in close proximity to the enemy EC
                // if adjacent to enemy EC, then hault the movement; sence and expose is the only task to do.
//                if (nextToEnemyEC()) {
//                    System.out.println("***** NEXT TO neutral EC **********");
//                    if (rc.canEmpower(actionRadius)) {
//                        System.out.println("empowering...");
//                        rc.empower(actionRadius);
//                        System.out.println("empowered");
//                        return;
//                    }
//                } else {
//                    if (rc.canEmpower(actionRadius)) {
//                        System.out.println("empowering...");
//                        rc.empower(actionRadius);
//                        System.out.println("empowered");
//                        return;
//                    }
//            if (rc.canEmpower(actionRadius)) {
//                System.out.println("empowering...");
//                rc.empower(actionRadius);
//                System.out.println("empowered");
//                            return;
//            }
                    Direction possibleDir = rc.getLocation().directionTo(neutralECLocation);
                    if (neutralECLocationSet && tryMove(possibleDir)) {
                        prevMovedDir = possibleDir;
                        System.out.println("poly Moved!");
                    } else {
                        Direction possibleDir1 = getHighPassableDirection();
                        if (tryMove(possibleDir1)) {
                            prevMovedDir = possibleDir1;
                            System.out.println("poly Moved!");
                        } else if (tryMove(randomDirection())) {
                            System.out.println("poly moved!");
                        }
                    }
            if (rc.canEmpower(actionRadius)) {
                System.out.println("empowering...");
                rc.empower(actionRadius);
                System.out.println("empowered");
//                return;
            }
//                }
            }

    }

    private static void neutralECFoundAction(RobotInfo robot, int i) throws GameActionException {
        neutralEcFound = true;
        neutralECLocation = robot.getLocation();
        neutralECLocationSet = true;
        neutralECDirection = rc.getLocation().directionTo(enemyECLocation);

        // set Flag to let other muck's know
        int flagValue = 111;
        if (rc.canSetFlag(flagValue))
            rc.setFlag(flagValue);
    }

    static void runSlanderer() throws GameActionException {
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }

    static void runMuckraker() throws GameActionException {
        boolean moved = false;
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots()) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    System.out.println("e x p o s e d");
                    rc.expose(robot.location);
                    return;
                }
            }
            if(robot.getTeam() != rc.getTeam() && robot.getType() == RobotType.SLANDERER && !moved) {
                Direction dir = rc.getLocation().directionTo(robot.getLocation());
                System.out.println("Slanderer spotted to the " + dir);
                tryMove(dir);
                moved = true;
            }
        }
        if(!moved)
            if (tryMove(randomDirection()))
                System.out.println("I moved!");
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }
    public static int myMod(int i, int j) {
        return (((i % j) + j) % j);
    }
    public static void enemySlandExpose(RobotInfo robot, int i) throws GameActionException {
        if (rc.canExpose(robot.location)) {
            System.out.println("e x p o s e d");
            rc.expose(robot.location);
            if(i == 1) {
                Direction possibleDir = rc.getLocation().directionTo(robot.getLocation());
                if (tryMove(possibleDir))
                    prevMovedDir = possibleDir;
                return;
            }
        }
    }
    public static boolean nextToEnemyEC() {
        MapLocation myLocation = rc.getLocation();
        int xLoc = myLocation.x;
        int yLoc = myLocation.y;
//        int xE = enemyECLocation.x;
//        int yE = enemyECLocation.y;
        int xE = neutralECLocation.x;
        int yE = neutralECLocation.y;
        if((xLoc-1 == xE && yLoc == yE) ||
                (xLoc+1 == xE && yLoc == yE) ||
                (xLoc == xE && yLoc+1 == yE) ||
                (xLoc == xE && yLoc-1 == yE) ||
                (xLoc+1 == xE && yLoc-1 == yE) ||
                (xLoc+1 == xE && yLoc+1 == yE) ||
                (xLoc-1 == xE && yLoc+1 == yE)||
                (xLoc-1 == xE && yLoc-1 == yE)) {
            return true;
        }
        return false;
    }
    public static Direction getHighPassableDirection() throws GameActionException {
        double maxPass = 0.0;
        Direction maxPassDir = randomDirection();
        for(Direction d: Direction.values()){
            if(d != prevMovedDir && rc.canSenseLocation(rc.adjacentLocation(d))){
                double currPass = rc.sensePassability(rc.adjacentLocation(d));
                if(currPass > maxPass){
                    maxPass = currPass;
                    maxPassDir = d;
                } } }
        return maxPassDir;
    }
    public static void enemyMuckFoundAction(RobotInfo robot, int i) throws GameActionException {
        int flagValue = 11300;
        if(rc.canSetFlag(flagValue))
            rc.setFlag(flagValue);
    }

    public static void enemyECFoundAction(RobotInfo robot, int i) throws GameActionException {
        enemyEcFound = true;
        enemyECLocation = robot.getLocation();
        enemyECLocationSet = true;
        enemyECDirection = rc.getLocation().directionTo(enemyECLocation);

        // set Flag to let other muck's know
        int flagValue = 11400;
        if (rc.canSetFlag(flagValue))
            rc.setFlag(flagValue);
    }
}
