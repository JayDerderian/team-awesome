package teamawesome;
import battlecode.common.*;
import static teamawesome.FlagConstants.*;

public strictfp class Muckraker extends RobotPlayer {

    /**
     * Variables of Muckraker
     */
    public String robotStatement = "I'm a " + rc.getType() + "! Location " + rc.getLocation();

    public MapLocation enemyECLocation;
    public Direction enemyECDirection;
    public Direction botDirectionToMove;
    public Direction prevMovedDir;
    public int xLean;
    public int yLean;
    public int dirIdx;
    public boolean enemyECLocationSet = false;
    public boolean enemyEcFound = false;

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

        for (RobotInfo robot : rc.senseNearbyRobots()) {
            // ENEMY
            if (robot.getTeam() == enemy) { // Slanderer
                if (robot.type.canBeExposed()) {
                    // It's a slanderer... go get them!
                    if (rc.canExpose(robot.location)) {
                        System.out.println("e x p o s e d");
                        rc.expose(robot.location);
                        Direction possibleDir = rc.getLocation().directionTo(robot.getLocation());
                        if(tryMove(possibleDir))
                            prevMovedDir = possibleDir;
                        return;
                    }
                }
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER) { // enemy EC
                    enemyEcFound = true;
                    enemyECLocation = robot.getLocation();
                    enemyECLocationSet = true;
                    enemyECDirection = rc.getLocation().directionTo(enemyECLocation);

                    // set Flag to let other muck's know
                    int flagValue = makeFlag(ENEMY_ENLIGHTENMENT_CENTER_FLAG, 0);
                    if (rc.canSetFlag(flagValue))
                        rc.setFlag(flagValue);

                    break;
                } else if(robot.type == RobotType.MUCKRAKER) {
                    int flagValue = makeFlag(ENEMY_MUCKRAKER_NEARBY_FLAG, 0);
                    if(rc.canSetFlag(flagValue))
                        rc.setFlag(flagValue);
                }
            } else if (robot.getTeam() != enemy) { // OUR TEAM

                if(rc.canGetFlag(robot.ID)) {
                    int flagValue = rc.getFlag(robot.ID);
                    if(flagValue == 11400) { // other muck near enemy EC, then move in that dir till u find enemy EC
                        botDirectionToMove = rc.getLocation().directionTo(robot.getLocation());
                        while(!enemyEcFound) {
                            if (tryMove(botDirectionToMove)) {
                                prevMovedDir = botDirectionToMove;
                                System.out.println("MUCK moved!");
                            }
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
                                        } }
                                    if (robot1.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                                        enemyEcFound = true;
                                        flagValue = makeFlag(ENEMY_ENLIGHTENMENT_CENTER_FLAG, 0);
                                        if (rc.canSetFlag(flagValue))
                                            rc.setFlag(flagValue);
                                    } else if(robot.type == RobotType.MUCKRAKER) {
                                        flagValue = makeFlag(ENEMY_MUCKRAKER_NEARBY_FLAG, 0);
                                        if(rc.canSetFlag(flagValue))
                                            rc.setFlag(flagValue);
                                    }
                                } else if (robot1.getTeam() != enemy && robot1.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                                    // reset Flag
                                    enemyEcFound = false;
                                    flagValue = 00000;
                                    if (rc.canSetFlag(flagValue))
                                        rc.setFlag(flagValue);
                                } } } } } }
        }

        // Move Muckraker
        if(!enemyEcFound) { // Initially explore map quickly (along with Slanders)
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
//            else {
//                // Clean the leans somewhat
//                if (Math.abs(xLean) > 2 * Math.abs(yLean)) {yLean = 0;}
//                else if (Math.abs(yLean) > 2 * Math.abs(xLean)) {xLean = 0;}
//                xLean = Math.min(1, Math.max(-1, xLean)) * -1;
//                yLean = Math.min(1, Math.max(-1, yLean)) * -1;
//                for (Direction dir : RobotPlayer.directions) {
//                    if (dir.getDeltaY() == yLean && dir.getDeltaX() == xLean) {
//                        if (rc.canMove(dir)) { rc.move(dir); }
//                        return;
//                    }
//                    if(enemyEcFound)
//                        break;
//                } }
        } else { // If enemy EC found, then move in close proximity to the enemy EC
            // if adjacent to enemy EC, then hault the movement; sence and expose is the only task to do.
            if(rc.adjacentLocation(rc.getLocation().directionTo(enemyECLocation)) == enemyECLocation){
                if(rc.canSetFlag(makeFlag(ENEMY_SLANDERER_NEARBY_FLAG, 0)))
                    rc.setFlag(makeFlag(ENEMY_SLANDERER_NEARBY_FLAG, 0));
                for (RobotInfo robot : rc.senseNearbyRobots()) {
                    // ENEMY
                    if (robot.getTeam() == enemy) { // Slanderer
                        if (robot.type.canBeExposed()) {
                            // It's a slanderer... go get them!
                            if (rc.canExpose(robot.location)) {
                                System.out.println("e x p o s e d");
                                rc.expose(robot.location);
//                            Direction possibleDir = rc.getLocation().directionTo(robot.getLocation());
//                            if (tryMove(possibleDir))
//                                prevMovedDir = possibleDir;
                                return;
                            }
                        }
                    }
                }
            }
            else {
            for (RobotInfo robot : rc.senseNearbyRobots()) {
                // ENEMY
                if (robot.getTeam() == enemy) { // Slanderer
                    if (robot.type.canBeExposed()) {
                        // It's a slanderer... go get them!
                        if (rc.canExpose(robot.location)) {
                            System.out.println("e x p o s e d");
                            rc.expose(robot.location);
//                            Direction possibleDir = rc.getLocation().directionTo(robot.getLocation());
//                            if (tryMove(possibleDir))
//                                prevMovedDir = possibleDir;
                            Direction possibleDir = rc.getLocation().directionTo(enemyECLocation);
                            if(enemyECLocationSet && tryMove(possibleDir)) {
                                prevMovedDir = possibleDir;
                                System.out.println("Muck Moved!");
                            }
                            else {
                                Direction possibleDir1 = getHighPassableDirection();
                                if (tryMove(possibleDir1)) {
                                    prevMovedDir = possibleDir1;
                                    System.out.println("Muck Moved!");
                                } else if (tryMove(randomDirection())) {
                                    System.out.println("Muck moved!");
                                }
                            }
                            return;
                        }
                    }
                }
            }
//            Direction possibleDir = rc.getLocation().directionTo(enemyECLocation);
//            if(enemyECLocationSet && tryMove(possibleDir)) {
//                prevMovedDir = possibleDir;
//                System.out.println("Muck Moved!");
//            }
//            else {
//                Direction possibleDir1 = getHighPassableDirection();
//                if (tryMove(possibleDir1)) {
//                    prevMovedDir = possibleDir1;
//                    System.out.println("Muck Moved!");
//                } else if (tryMove(randomDirection())) {
//                    System.out.println("Muck moved!");
//                }
//            }
            }
        } }

    private Direction getHighPassableDirection() throws GameActionException {
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

    public int myMod(int i, int j) {
        return (((i % j) + j) % j);
    }
}