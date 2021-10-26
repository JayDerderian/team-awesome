package teamawesome;

import battlecode.common.*;

import java.awt.*;

public class Slanderer extends GenericRobot{
    int ecID = -1;
    private MapLocation ecLoc;

    private MapLocation enemyECLoc;

    private Direction dir;

    public Slanderer(RobotController newRc) {
        super(newRc);
    }

    // Slanderers will run away from the enemy. If they detect a EC nearby, they will try to reach it.

    int getECID() throws GameActionException{
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER && robot.team == rc.getTeam()) {
                return robot.ID;
            }
        }
        throw new GameActionException(GameActionExceptionType.CANT_DO_THAT, "Can't get EC");
    }

    MapLocation getLocationOfEC() throws GameActionException{
        RobotInfo [] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER && robot.team == rc.getTeam()) {
                return robot.location;
            }
        }
        throw new GameActionException(GameActionExceptionType.OUT_OF_RANGE, "Can't reach EC");
    }

    void turn() throws GameActionException {
        /*
        if (ecID == -1) {
            // badaid
            try {
                ecID = getECID();
            }catch(GameActionException e){
                ecID = 0;
            }
            //ecLoc = getLocationOfEC();
        }
        */
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }


    void moveAway() throws GameActionException {
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
        RobotInfo[] closeRobots = rc.senseNearbyRobots(2, rc.getTeam());
        MapLocation ecLocTeam = rc.adjacentLocation(Direction.NORTH); // problem?
        boolean closeToEC = false;
        for (RobotInfo robot : closeRobots) {
            if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                closeToEC = true;
                ecLocTeam = robot.location;
            }
        }

        // Do something if enemy is nearby

        // Do something if the EC is nearby

    }
}