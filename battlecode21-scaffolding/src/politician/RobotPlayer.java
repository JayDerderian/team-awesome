package politician;
import battlecode.common.*;

import java.util.LinkedList;
import java.util.List;

public class RobotPlayer {

    static RobotController rc;
    public void run(RobotController rc) throws GameActionException {
        System.out.println("I'm a politician from the new file!");
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
        MapLocation next = mostPassable();
        Direction d = next.directionTo(rc.getLocation());
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
    public MapLocation mostPassable() throws GameActionException {
        int[] x = {-1, 0, 1};
        int[] y = {-1, 0, 1};
        MapLocation currentLocation = rc.getLocation();
        LinkedList<MapLocation> locations = new LinkedList<>();
        for(int xi : x) {
            for(int yi : y) {
                if(xi == 0 && yi == 0) {
                    // do nothing
                } else {
                    locations.add(new MapLocation(currentLocation.x + xi, currentLocation.y + yi));
                }
            }
        }
        MapLocation toReturn = locations.get(0);
        double maxPass = rc.sensePassability(toReturn);;
        for (MapLocation m:
             locations) {
            double thisPass = rc.sensePassability(m);
            if(thisPass > maxPass) {
                maxPass = thisPass;
                toReturn = m;
            }
        }
        return toReturn;
    }
}
