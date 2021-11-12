package teamawesome;
import battlecode.common.*;
import static teamawesome.FlagConstants.*;

public strictfp class Muckraker extends GenericRobot {

    /**
     * Variables of Muckraker
     */
    public String robotStatement = "I'm a " + rc.getType() + "! Location " + rc.getLocation();

    public MapLocation enemyECLocation;
    public Direction enemyECDirection;
//    boolean enemyECLocationSet = false;
    boolean enemyEcFound = false;
//    boolean botDirectionToMoveSet = false;
    Direction botDirectionToMove;
    public int xLean;
    public int yLean;
    public int dirIdx;
    public RobotInfo[] nearby;

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
        xLean = 0; yLean = 0; // Reset guiding
        System.out.println(robotStatement);
        Team enemy = rc.getTeam().opponent();
//        RobotInfo[] sensedNearByRobots = rc.senseNearbyRobots();

        for (RobotInfo robot : rc.senseNearbyRobots()) {
            // ENEMY
            if (robot.getTeam() == enemy) { // Slanderer
                if (robot.type.canBeExposed()) {
                    // It's a slanderer... go get them!
                    if (rc.canExpose(robot.location)) {
                        System.out.println("e x p o s e d");
                        rc.expose(robot.location);
                        tryMove(rc.getLocation().directionTo(robot.getLocation()));
                        return;
                    }
                }
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                    enemyEcFound = true;
                    enemyECLocation = robot.getLocation();
                    enemyECDirection = rc.getLocation().directionTo(enemyECLocation);

                    // set Flag to let other muck's know
                    int flagValue = makeFlag(ENEMY_ENLIGHTENMENT_CENTER_FLAG, 0);
                    if (rc.canSetFlag(flagValue))
                        rc.setFlag(flagValue);

                    break;
                }
            } else if (robot.getTeam() != enemy){
                if(rc.canGetFlag(robot.ID)) {
                    int flagValue = rc.getFlag(robot.ID);
                    if(flagValue == 11400){
                        botDirectionToMove = rc.getLocation().directionTo(robot.getLocation());
//                        botDirectionToMoveSet = true;
                        while(!enemyEcFound) {
                            if (tryMove(botDirectionToMove))
                                System.out.println("MUCK moved!");
                            else
                                break;
                            for (RobotInfo robot1 : rc.senseNearbyRobots()) {
                                if (robot1.getTeam() == enemy) {
                                    if (robot1.type.canBeExposed()) {
                                        // It's a slanderer... go get them!
                                        if (rc.canExpose(robot1.location)) {
                                            System.out.println("e x p o s e d");
                                            rc.expose(robot1.location);
                                            return;
                                        }
                                    }
                                }
                                if (robot1.getTeam() == enemy && robot1.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                                    enemyEcFound = true;
                                    flagValue = makeFlag(ENEMY_ENLIGHTENMENT_CENTER_FLAG, 0);
                                    if (rc.canSetFlag(flagValue))
                                        rc.setFlag(flagValue);
                                }
                            }
                        }
                    }
                }
            }

        }

        // Move
        if(!enemyEcFound) {
//            nearby = rc.senseNearbyRobots();
//            if (nearby.length == 0)
//                return;
//            int x = rc.getLocation().x;
//            int y = rc.getLocation().y;
//            for (RobotInfo robot : nearby) {
//                if (robot.getTeam() == rc.getTeam().opponent()) {
//                    xLean += robot.getLocation().x - x;
//                    yLean += robot.getLocation().y - y;
//                }
//            }
            if (xLean == 0 && yLean == 0) {
                int[] x1 = {0, 1, -1, 3, -3, 2, -2, 4, -4};
                for (int i: x1) {
                    if (rc.canMove(RobotPlayer.directions[myMod((dirIdx + i), RobotPlayer.directions.length)])) {
                        rc.move(RobotPlayer.directions[myMod((dirIdx + i), RobotPlayer.directions.length)]);
                        dirIdx += i;
                        break;
                    }
                    if(enemyEcFound)
                        break;
                }
            }
            else {
                // Clean the leans somewhat
                if (Math.abs(xLean) > 2 * Math.abs(yLean)) {yLean = 0;}
                else if (Math.abs(yLean) > 2 * Math.abs(xLean)) {xLean = 0;}
                xLean = Math.min(1, Math.max(-1, xLean)) * -1;
                yLean = Math.min(1, Math.max(-1, yLean)) * -1;
                for (Direction dir : RobotPlayer.directions) {
                    if (dir.getDeltaY() == yLean && dir.getDeltaX() == xLean) {
//                        System.out.println("I'm moving to " + dir);
                        if (rc.canMove(dir)) { rc.move(dir); }
                        return;
                    }
                    if(enemyEcFound)
                        break;
                }
//                System.out.println("Cannot Move!!!");
            }
        } else {
            if(tryMove(rc.getLocation().directionTo(enemyECLocation))){
                System.out.println("Muck Moved!");
                return;
            } else {
                Direction leastPassabilityDirection = getLeastPassableDirection();
                if(tryMove(leastPassabilityDirection)) {
                    System.out.println("Muck Moved!");
                } else {
                    if (tryMove(randomDirection())) {
                        System.out.println("Muck moved!");
                    }
                }
            }
        }
    }

    private Direction getLeastPassableDirection() throws GameActionException {
        double minPass = 1.0;
        Direction minPassDir = randomDirection();
        for(Direction d: Direction.values()){
//            if(hasPrevMovedDir && prevMovedDir.opposite() != d){
                double currPass = rc.sensePassability(rc.adjacentLocation(d));
                if(currPass < minPass){
                    minPass = currPass;
                    minPassDir = d;
                }
//            }
        }
        return minPassDir;
    }

    public int myMod(int i, int j) {
        return (((i % j) + j) % j);
    }
}
