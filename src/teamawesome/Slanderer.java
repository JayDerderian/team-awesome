package teamawesome;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Collections;

import static teamawesome.FlagConstants.*;

public class Slanderer extends RobotPlayer {

    public String robotStatement = "I'm a " + rc.getType() + "! Location " + rc.getLocation();
    public int dirIdx;

    public int xLean;
    public int yLean;

    public RobotInfo[] nearby;

    public final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    public Slanderer(RobotController newRc) {
        super(newRc);
    }

    // Setup the slanderer
    public void setup() throws GameActionException {
        dirIdx = (int) (Math.random() * directions.length);

    }

    // Run the slanderer
    public void turn() throws GameActionException {
        hasSetFlag = false;
        xLean = 0; yLean = 0; // Reset guiding
        analyze();
        move();
        //int flag = genFlagNearestEC();
        //System.out.println("My Flag is " + flag);
        //if (rc.canSetFlag(flag)) { rc.setFlag(flag); }
        //else { System.out.println("Can't set it to that!"); }
        if(!hasSetFlag && rc.canSetFlag(FlagConstants.NEUTRAL)) rc.setFlag(NEUTRAL);
    }

    public void analyze() throws GameActionException {
        nearby = rc.senseNearbyRobots();
        if (nearby.length == 0) {
            System.out.println("No one nearby.");
            return;
        }
        int x = 0, y = 0;
        if (rc.getLocation() != null) {
            x = rc.getLocation().x;
            y = rc.getLocation().y;
        }
        for (RobotInfo robot : nearby) {
            if (robot.getTeam() == rc.getTeam().opponent()) {
                xLean += robot.getLocation().x - x;
                yLean += robot.getLocation().y - y;
            } else if(robot.getTeam() == Team.NEUTRAL) {
                txLocation(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG, robot.getLocation(), 0);
            }
        }
    }

    public void move() throws GameActionException {
        // Random movement if not leans
        if (xLean == 0 && yLean == 0) {
            int[] x = {0, 1, -1, 3, -3, 2, -2, 4, -4};
            for (int i: x) {
                if (rc.canMove(directions[myMod((dirIdx + i), directions.length)])) {
                    rc.move(directions[myMod((dirIdx + i), directions.length)]);
                    dirIdx += i;
                    System.out.println("I'm moving randomly");
                    break;
                }
            }
        }
        else {
            // Clean the leans somewhat
            if (Math.abs(xLean) > 2 * Math.abs(yLean)) {yLean = 0;}
            else if (Math.abs(yLean) > 2 * Math.abs(xLean)) {xLean = 0;}
            xLean = Math.min(1, Math.max(-1, xLean)) * -1;
            yLean = Math.min(1, Math.max(-1, yLean)) * -1;
            for (Direction dir : directions) {
                if (dir.getDeltaY() == yLean && dir.getDeltaX() == xLean) {
                    System.out.println("I'm moving to " + dir);
                    if (rc.canMove(dir)) { rc.move(dir); }
                    return;
                }
            }
            System.out.println("Cannot Move!!!");
        }
    }

    public int genFlagNearestEC() throws GameActionException {
        RobotInfo[] all = rc.senseNearbyRobots();
        ArrayList<MapLocation> nearby = new ArrayList<>();
        for (RobotInfo robot: all) {
            if (robot.getType() == RobotType.ENLIGHTENMENT_CENTER && robot.getTeam() == rc.getTeam().opponent()) {
                nearby.add(robot.getLocation());
            }
            else if (robot.getTeam() == rc.getTeam()) {
                MapLocation q = parseFlagNearestEC(robot);
                if (q != null) { nearby.add(q); }
            }
        }

        if (nearby.isEmpty()) {
            return 0;
        }
        else {
            Collections.sort(nearby, (ml1, ml2) -> (rc.getLocation().distanceSquaredTo(ml1) - rc.getLocation().distanceSquaredTo(ml2)));
            MapLocation nearest = nearby.get(0);
            // format: NXXYY
            // N = 0 (+, +), 1 (-, +), 2 (+, -), 3 (-, -)
            int i = 0;
            int x = rc.getLocation().x; int y = rc.getLocation().y;
            if (nearest.x - x <= 0 && nearest.y - y <= 0) { i = 3; }
            else if (nearest.x - x <= 0) { i = 1; }
            else if (nearest.y - y <= 0) { i = 2; }

            return i * 10000 + Math.abs(nearest.x - x) * 100 + Math.abs(nearest.y - y);
        }
    }

    public MapLocation parseFlagNearestEC(RobotInfo robot) throws GameActionException {
        if (rc.canGetFlag(robot.getID())) {
            int flag = rc.getFlag(robot.getID());
            if (flag != 0) {
                int xModifier = 1; int yModifier = 1;
                if (flag >= 30000) { xModifier = -1; yModifier = -1; flag -= 30000; }
                else if (flag >= 20000) { yModifier = -1; flag -= 20000; }
                else if (flag >= 10000) { xModifier = -1; flag -= 10000; }


                int ones = flag % 10;
                int tens = (int)((flag % 100) / 10);
                int hundreds = (int)((flag % 1000) / 100);
                int thousands = (int)((flag % 10000) / 1000);

                int x = robot.getLocation().x + ((thousands * 10 + hundreds) * xModifier);
                int y = robot.getLocation().y + ((tens * 10 + ones) * yModifier);

                return new MapLocation(x, y);
            }
        }
        return null;
    }

    public int myMod(int i, int j) {
        return (((i % j) + j) % j);
    }
}