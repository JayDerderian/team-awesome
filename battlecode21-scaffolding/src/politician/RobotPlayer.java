package politician;
import battlecode.common.*;

import javax.xml.stream.Location;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class RobotPlayer {

    static RobotController rc;
    LinkedList<MapLocation> history;
    public RobotPlayer(RobotController newRc) {
        rc = newRc;
        history = new LinkedList<>();
    }

    public void run() throws GameActionException {
        System.out.println("I'm a politician from the new file!");
        Team enemy = RobotPlayer.rc.getTeam().opponent();
        int actionRadius = RobotPlayer.rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            System.out.println("empowering...");
            rc.empower(actionRadius);
            System.out.println("empowered");
            return;
        }
        // then try to move
        Direction d = whereToMove();
        if(RobotPlayer.rc.canMove(d)) {
            RobotPlayer.rc.move(d);
            System.out.println("I Moved! Direction: " + d.toString());
        } else {
            System.out.println("I'm Stuck!");
        }
    }

    /*
    Find the most passable square
     */
    public Direction whereToMove() throws GameActionException {
        history.add(rc.getLocation());
        if(history.size() > 10) {
            history.removeLast();
        }
        HashMap<Direction, Double> locations = new HashMap<>();
        double maxPass = 0;
        MapLocation toReturn = null;
        Direction toMove = null;
        // check the squares in each direction for passability
        // this forms the basis for direction weights
        for(Direction d:
            Direction.values()) {
            MapLocation thisLocation = rc.adjacentLocation(d);
            if(rc.onTheMap(thisLocation) && d != Direction.CENTER) {
                double thisPass = rc.sensePassability(thisLocation);
                locations.put(d, thisPass);
            }
        }
        // check nearby robots
        RobotInfo[] info = rc.senseNearbyRobots();
        // update weights of different directions depending on what robots are that way
        for (RobotInfo robot:
             info) {
            Direction robotDirection = rc.getLocation().directionTo(robot.getLocation());
            double dirWeight = locations.get(robotDirection);
            // prefer to go toward enemy robots
            if(robot.getTeam() != rc.getTeam()) {
                dirWeight += 1;
                if(robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    dirWeight += 1;
                }
            } else {
                dirWeight -= 0.5;
            }
            locations.put(robotDirection, dirWeight);
        }
        // prefer to go away from the way we came
        for(MapLocation l :
            history) {
            if(rc.getLocation().directionTo(l) != Direction.CENTER) {
                Direction histDirection = rc.getLocation().directionTo(l);
                locations.put(histDirection, locations.get(histDirection) - 0.25);
            }
        }
        // select the direction with the highest weight
        Map.Entry<Direction, Double> best = null;
        for (Map.Entry<Direction, Double> entry:
             locations.entrySet()) {
            if(best == null || best.getValue() < entry.getValue()) {
                best = entry;
            }
        }
        if(best == null) {
            toMove = Direction.EAST;
        } else {
            toMove = best.getKey();
        }
        System.out.println("I want to move " + toMove + " to space " + rc.adjacentLocation(toMove));
        return toMove;
    }
}
