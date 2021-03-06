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
    public Direction dirCreated;
    public int xLean, yLean, dirIdx, flagValue;
    public int maxIter = 50;
    public boolean enemyECLocationSet = false;
    public boolean enemyEcFound = false;
    public boolean muckJuggernaut =  false;

    /**
     * constructor
     * @param newRc - new RobotController Object
     */
    public Muckraker(RobotController newRc) {
        super(newRc);
        if(rc.getInfluence() == 2) {
            muckJuggernaut = true;
            dirCreated = rc.getLocation().directionTo(motherLoc).opposite();
        }
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
            if (robot.getTeam() == enemy) { // Slanderer; enemy EC
                if (robot.type.canBeExposed() && !muckJuggernaut) enemySlandExpose(robot, 1);
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                    enemyECFoundAction(robot, 1);
                    break;
                }
            }
            else if (robot.getTeam() == rc.getTeam()) { // OUR TEAM
                if(rc.canGetFlag(robot.ID)) {
                    flagValue = rc.getFlag(robot.ID);
                    if(flagValue == 11400) { // other muck near enemy EC, then move in that dir till u find enemy EC
                        botDirectionToMove = rc.getLocation().directionTo(robot.getLocation());
                        while(!enemyEcFound && maxIter > 0) {
                            if (tryMove(botDirectionToMove)) {
                                prevMovedDir = botDirectionToMove;
                                System.out.println("MUCK moved!");
                            }
                            else break;
                            for (RobotInfo robot1 : rc.senseNearbyRobots()) {
                                if (robot1.getTeam() == enemy) {
                                    if (robot1.type.canBeExposed()) enemySlandExpose(robot1, 2);
                                    if (robot1.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                                        enemyECFoundAction(robot1, 2);
                                        break;
                                    }
                                } else if (robot1.getTeam() == rc.getTeam() && robot1.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                                    resetFlagAndMove(0);
                                    break;
                                } }
                            maxIter--;
                        } } } }
                else { // If Neutral EC
                    if(robot.getType() == RobotType.ENLIGHTENMENT_CENTER) txLocation(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG, robot.getLocation(), 0);
            }
        }

        // Move Muckraker
        if(!enemyEcFound && !muckJuggernaut) { // Initially explore map quickly (along with Slanders)
            int[] x1 = {0, 1, -1, 3, -3, 2, -2, 4, -4};
            for (int i :x1) {
                if (rc.canMove(RobotPlayer.directions[myMod((dirIdx + i), RobotPlayer.directions.length)])) {
                    rc.move(RobotPlayer.directions[myMod((dirIdx + i), RobotPlayer.directions.length)]);
                    dirIdx += i;
                    break;
                }
                if(enemyEcFound) break;
            }
        } else if (!enemyEcFound && muckJuggernaut) {
                if(!rc.onTheMap(rc.adjacentLocation(dirCreated))) dirCreated = randomDirection();
                if (tryMove(dirCreated)) System.out.println("Muck Moved!");
                else tryMove(randomDirection());
        } else { // If enemy EC found, then move in close proximity to the enemy EC
            // if adjacent to enemy EC, then halt the movement; sense and expose is the only task to do.
            if (nextToEnemyEC()) {
                System.out.println("NEXT TO ENEMY EC");
                enemyEcDetectedMuck(1);
            }
            else {
                enemyEcDetectedMuck(0);
                Direction possibleDir = rc.getLocation().directionTo(enemyECLocation);
                if (enemyECLocationSet && tryMove(possibleDir)) {
                    prevMovedDir = possibleDir;
                    System.out.println("Muck Moved!");
                } else {
                    resetFlagAndMove(1);
                }
            }
        }
    }

    private void enemyEcDetectedMuck(int i) throws GameActionException {
        for (RobotInfo robot : rc.senseNearbyRobots()) {
            // ENEMY converted to Our team
            if (robot.getTeam() == rc.getTeam().opponent()) { // Slanderer
                if (robot.type.canBeExposed()) { // It's a slanderer... go get them!
                    enemySlandExpose(robot, 3);
                }
            }
            if (robot.getTeam() == rc.getTeam() && robot.getType() == RobotType.ENLIGHTENMENT_CENTER) { // enemy EC converted to our team EC
                resetFlagAndMove(0);
                break;
            }
            else if(i == 1 && robot.getTeam() == rc.getTeam()) { // if our team politicians nearby to empower, given them way.
                if(robot.getType() == RobotType.POLITICIAN) {
                    Direction possibleDir = rc.getLocation().directionTo(enemyECLocation);
                    if (enemyECLocationSet && tryMove(possibleDir)) {
                        prevMovedDir = possibleDir;
                        System.out.println("Muck Moved!");
                    } else {
                        Direction possibleDir1 = getHighPassableDirection();
                        if (tryMove(possibleDir1)) {
                            prevMovedDir = possibleDir1;
                            System.out.println("Muck Moved!");
                        } else if (tryMove(randomDirection())) System.out.println("Muck moved!");
                    }
                } }
        }
    }

    private void resetFlagAndMove(int i) throws GameActionException {
        if(i == 0) {
            // reset Flag
            enemyEcFound = false;
            enemyECLocationSet = false;
            flagValue = 00000;
            if (rc.canSetFlag(flagValue))
                rc.setFlag(flagValue);
        }
        Direction possibleDir1 = getHighPassableDirection();
        if (tryMove(possibleDir1)) {
            prevMovedDir = possibleDir1;
            System.out.println("Muck Moved!");
        } else if (tryMove(randomDirection())) {
            System.out.println("Muck moved!");
        }
    }

    public void enemyECFoundAction(RobotInfo robot, int i) throws GameActionException {
        enemyEcFound = true;
        enemyECLocation = robot.getLocation();
        enemyECLocationSet = true;
        enemyECDirection = rc.getLocation().directionTo(enemyECLocation);

        // set Flag to let other muck's know
        txLocation(ENEMY_ENLIGHTENMENT_CENTER_FLAG, enemyECLocation, 0);
//        flagValue = makeFlag(ENEMY_ENLIGHTENMENT_CENTER_FLAG, 0); // 11400
//        if (rc.canSetFlag(flagValue))
//            rc.setFlag(flagValue);
    }

    public void enemySlandExpose(RobotInfo robot, int i) throws GameActionException {
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

    public boolean nextToEnemyEC() {
        MapLocation myLocation = rc.getLocation();
        int xLoc = myLocation.x;
        int yLoc = myLocation.y;
        int xE = enemyECLocation.x;
        int yE = enemyECLocation.y;
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

    public Direction getHighPassableDirection() throws GameActionException {
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