package politician;
import battlecode.common.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RobotPlayer {

    static RobotController rc;
    public void run(RobotController rc) throws GameActionException {
        System.out.println("I'm a politician from the new file!");
        RobotPlayer.rc = rc;
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            System.out.println("empowering...");
            rc.empower(actionRadius);
            System.out.println("empowered");
            return;
        }
        // then try to move
        Direction d = mostPassable();
        if(rc.canMove(d)) {
            rc.move(d);
            System.out.println("I Moved! Direction: " + d.toString());
        } else {
            System.out.println("I'm Stuck!");
        }
    }

    /*
    Find the most passable square
     */
    public Direction mostPassable() throws GameActionException {
        HashMap<Direction, MapLocation> locations = new HashMap<>();
        double maxPass = 0;
        MapLocation toReturn = null;
        Direction toMove = null;
        // check the squares in each direction for passability
        for(Direction d:
            Direction.values()) {
            MapLocation thisLocation = rc.adjacentLocation(d);
            if(rc.onTheMap(thisLocation)) {
                double thisPass = rc.sensePassability(thisLocation);
                if(thisPass > maxPass) {
                    toReturn = thisLocation;
                    maxPass = thisPass;
                    toMove = d;
                }
            }
        }
        System.out.println("I want to move " + toMove + " to space " + toReturn);
        return toMove;
    }
}
