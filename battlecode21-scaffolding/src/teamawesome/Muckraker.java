package teamawesome;
import battlecode.common.*;
import static teamawesome.FlagConstants.*;

public strictfp class Muckraker {
    static RobotController rc;

//    static final int NEUTRAL_ENLIGHTENMENT_CENTER_FLAG = 50;
//    static final int SLANDERER_FLAG = 102;

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

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

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
                    case ENLIGHTENMENT_CENTER: runEnlightenmentCenter(); break;
                    case POLITICIAN:           runPolitician();          break;
                    case SLANDERER:            runSlanderer();           break;
                    case MUCKRAKER:            runMuckraker();           break;
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
        RobotType toBuild = randomSpawnableRobotType();
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
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            System.out.println("empowering...");
            rc.empower(actionRadius);
            System.out.println("empowered");
            return;
        }
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }

    static void runSlanderer() throws GameActionException {
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }

    /**
     * created Muckraker
     * 1. sense every Robot --> If enemy
     *                              slanderer --> then expose.
     *                              EC -->  then set an 'Enemy EC' flag
     *                              Muckraker / politician --> do nothing
     *                      --> If Neutral EC --> set 'Neutral EC' Flag
     *
     * 2. Did not sense Robot/ already Exposed enemies --> then Detect
     *      detect surrounding. a. Found some robot --> Move in that direction
     *                          b. No Robot found --> choose Random Direction with low passability.
     */
    static void runMuckraker() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        boolean DetectEnemySlanderer = false;
        Direction detectedDirection = Direction.CENTER; // random value, change later

        // 1. Sense Every Robot (max actionRadiusSquared)
        for (RobotInfo robot : rc.senseNearbyRobots()) {
            // ENEMY
            if(robot.getTeam() == enemy){
                if (robot.type.canBeExposed()) {
                    // It's a slanderer... go get them!
                    if (rc.canExpose(robot.location)) {
                        System.out.println("e x p o s e d");
                        rc.expose(robot.location);
                        return;
                    }
                }
            }
            // NOT ENEMY
            else if(robot.getTeam() != enemy){
                if(robot.getTeam()!= rc.getTeam() && robot.getType() == RobotType.ENLIGHTENMENT_CENTER){ // can sense Neutral EC
                    // If Neutral EC nearby, get its location and set flag.
                    robot.getLocation();
                    rc.setFlag(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG);
                }
                else if (rc.canGetFlag(robot.getID())){
                    // If Same Team Robots, then get Flag and do appropriate action.
                    int flagSensed = rc.getFlag(robot.getID()); // what info flag is telling

                    if(flagSensed == NEUTRAL_ENLIGHTENMENT_CENTER_FLAG){
                        // if Neutral EC nearby sensed robot, set the flag to same value.
                        rc.setFlag(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG);
                    }
                    else if(flagSensed == ENEMY_SLANDERER_NEARBY_FLAG){
                        // if enemy slanderer, nearby sensed robot, retrieve direction/ location from that flag
                        // set Direction_of_Muckraker to that detected value
                        DetectEnemySlanderer = true;
                    }
                }
            }
        }

        // 2. Move in Random and explore map (or) if Direction of slanderer detected, then move in that direction.
        if(DetectEnemySlanderer){
            // tryMove(detectedDirection);
        } else if (tryMove(randomDirection())){
            System.out.println("I moved!");
        }
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
}
