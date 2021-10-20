package teamawesome;
import battlecode.common.*;

import javax.xml.stream.Location;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Politician {

    static RobotController rc;
    LinkedList<MapLocation> history;
    HashMap<Direction, Double> momentum;
    int mothership = -1;
    int homeFlag = -1;
    public Politician(RobotController newRc) {
        rc = newRc;
        history = new LinkedList<>();
        // initialize momentum to 0 in all directions
        momentum = new HashMap<>();
        for (Direction d:
                Direction.values()) {
            if(d != Direction.CENTER) momentum.put(d, 0.0);
        }
    }

    public void run() throws GameActionException {
        // check mothership for flag value
        if(mothership != -1) homeFlag = rc.getFlag(mothership);
        System.out.println("I'm a politician! My mothership is " + mothership + " and their flag is " + homeFlag);
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
        // then try to move
        Direction d = whereToMove();
        if(RobotPlayer.rc.canMove(d)) {
            // move and increment the momentum for that direction
            RobotPlayer.rc.move(d);
            System.out.println("I Moved! Direction: " + d.toString() + " | Momentum: " + momentum.get(d));
            momentum.put(d, momentum.get(d) + 1);
        } else {
            // if the robot can't move that direction, degrade momentum
            System.out.println("I'm Stuck! | Momentum: " + momentum.get(d));
            momentum.put(d, momentum.get(d) - 1);

        }
    }

    /*
    Find the most passable square
     */
    public Direction whereToMove() throws GameActionException {
        // record this location in the history list
        history.add(rc.getLocation());
        if(history.size() > 10) {
            history.removeLast();
        }
        // degrade momentum values
        for (Map.Entry<Direction, Double> e:
                momentum.entrySet()) {
            e.setValue(e.getValue() * 0.9);
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
                // if the direction is valid, set the weight to the passability of the square plus the momentum
                double thisPass = rc.sensePassability(thisLocation);
                if(momentum.containsKey(d)) thisPass += momentum.get(d);
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
                if(robot.getType() == RobotType.ENLIGHTENMENT_CENTER) mothership = robot.getID();
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
        StringBuilder s = new StringBuilder();
        for (Map.Entry<Direction, Double> entry:
                locations.entrySet()) {
            s.append(" | ").append(entry.getKey()).append(": ").append(String.format("%.2f", entry.getValue()));
            if(best == null || best.getValue() < entry.getValue()) {
                best = entry;
            }
        }
        System.out.println("My Weights Are: " + s + " |");
        if(best == null) {
            toMove = Direction.EAST;
        } else {
            toMove = best.getKey();
        }
        System.out.println("So I want to move " + toMove + " to space " + rc.adjacentLocation(toMove));
        return toMove;
    }
}
