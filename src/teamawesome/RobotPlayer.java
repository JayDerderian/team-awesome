package teamawesome;
import battlecode.common.*;

/**
 * RobotPlayer
 * This is the class that is called by the client environment to invoke a new robot
 * this class is static but its methods instantiate the robot objects
 */
public strictfp class RobotPlayer {
    static RobotController rc;

    // list of robots that can be spawned - ECs cannot be spawned
    public static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };

    // list of usable directions - center is omitted because for most game
    // actions center is irrelevant
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

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;
        Politician politic = new Politician(rc);
        turnCount = 0;
        GenericRobot robot; // the identity for this robot
        // initialize the robot identity according to the type stored in rc
        switch (rc.getType()) {
            case ENLIGHTENMENT_CENTER: robot = new EnlightenmentCenter(rc); break;
            case POLITICIAN:           robot = new Politician(rc);          break;
            case SLANDERER:            robot = new Slanderer(rc);           break;
            case MUCKRAKER:            robot = new EnlightenmentCenter(rc);           break;
            default:
                throw new IllegalStateException("Unexpected value: " + rc.getType());
        }

        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            // special case: slanderers become politicians after some time
            if(robot.getClass() == Slanderer.class && rc.getType() == RobotType.POLITICIAN) {
                robot = new Politician(robot.rc); // remake this robot as a politician
            }
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER:
                    case POLITICIAN:
                    case SLANDERER:            robot.turn();             break;
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

    static void runMuckraker() throws GameActionException {
        Muckraker.runMuckraker(rc);
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
