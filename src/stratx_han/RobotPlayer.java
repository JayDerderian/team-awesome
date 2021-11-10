package stratx_han;
import battlecode.common.*;
import teamawesome.Muckraker;
import teamawesome.Politician;
import teamawesome.Slanderer;

/**
 * Strategy experiment - sprint 3
 *
 * Variant of the examplefuncsplayer intended to explore counter strategies to our robot
 * Strategy:
 *  - toBuild influence level is the max of 50 or 10% of the EC's current influence
 *  - Build only slanderers for the first 300 rounds
 *     - except every 10th round build a muckraker
 *  - Build only politicians for the next 300 rounds
 *  - use randomspawnablerobot after that
 *  - randomspawnablerobot is weighted:
 *      - 20% muckraker
 *      - 30% slanderer
 *      - 50% politician
 *  - do not bid until the 750th round, then bid 5% of EC's influence each turn
 *  - slanderers do not ever leave range of another friendly robot
 */

public strictfp class RobotPlayer {
    public static final int BID_START = 600;
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
    protected static final int POLI_RUSH = 300;

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
        Politician poli = new Politician(rc);
        Muckraker muck = new Muckraker(rc);

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
                    case POLITICIAN:           poli.turn();              break;
                    case SLANDERER:            runSlanderer();           break;
                    case MUCKRAKER:            muck.turn();           break;
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
            if(rc.getRoundNum() % 10 != 0 )
                toBuild = RobotType.SLANDERER;
            else
                toBuild = RobotType.MUCKRAKER;
        } else if(rc.getRoundNum() < SLAN_RUSH + POLI_RUSH) {
            toBuild = RobotType.POLITICIAN;
        }
        else {
            toBuild = randomSpawnableRobotType();
        }
        int influence = (int)Math.max(50, 0.10 * rc.getInfluence());
        for (Direction dir : directions) {
            if (rc.canBuildRobot(toBuild, dir, influence)) {
                rc.buildRobot(toBuild, dir, influence);
            } else {
                break;
            }
        }
        int toBid = (int)(0.05 * rc.getInfluence());
        // don't bid before the mid game
        if(rc.canBid(toBid) && rc.getRoundNum() > BID_START) rc.bid(toBid);
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
        } else {
            RobotInfo[] convertable = rc.senseNearbyRobots(actionRadius, Team.NEUTRAL);
            if(convertable.length != 0 && rc.canEmpower(actionRadius)) {
                System.out.println("you will be assimilated");
                rc.empower(actionRadius);
            }
        }
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }

    static void runSlanderer() throws GameActionException {
        if(rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam()) == null)
            return;
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }

    static void runMuckraker() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    System.out.println("e x p o s e d");
                    rc.expose(robot.location);
                    return;
                }
            }
        }
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
        int diceRoll = (int) (Math.random() * 10);
        if(diceRoll == 2 || diceRoll == 4 || diceRoll == 6) {
            return RobotType.MUCKRAKER;
        }
        if(diceRoll % 2 == 0) {
            return RobotType.SLANDERER;
        }
        return RobotType.POLITICIAN;
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
