package teamawesome;

import battlecode.common.*;

import java.awt.*;
import java.util.ArrayList;

public class Slanderer extends GenericRobot{
    static int direction;
    static int x;
    static int y;

    static RobotInfo [] nearbyRobots;
    static RobotController rc;

    static final Direction[] directions = {Direction.NORTH, Direction.NORTHEAST, Direction.NORTHWEST, Direction.SOUTH, Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.EAST, Direction.WEST};


    public Slanderer(RobotController newRc) {
        super(newRc);
    }

//    static void getDirection() throws GameActionException {
//        direction = this.randomDirection();
//    }

    void turn() throws GameActionException {
        x = 0;
        y = 0;
        analyzeMove();
        move();
    }

    static void analyzeMove() throws GameActionException {
        nearbyRobots = rc.senseNearbyRobots();
        if (nearbyRobots.length == 0) {
            System.out.println("i'm alone");
            return;
        }
        int xDir = rc.getLocation().x;
        int yDir = rc.getLocation().y;
        for (RobotInfo robot : nearbyRobots) {
            if (robot.getTeam() == rc.getTeam().opponent()) {
                x += robot.getLocation().x - xDir;
                y += robot.getLocation().y - yDir;
            }
        }
    }

    static void move() throws GameActionException {
        // Move randomly if no one nearby
        if (x == 0 && y == 0) {
            int [] dir = {0, 1, -1, 2, -2, 3, -3, 4, -4};
            for (int i : dir) {
                if (rc.canMove(directions[generateRandomDirection((direction + 1), directions.length)])) {
                    rc.move(directions[generateRandomDirection((direction + 1), directions.length)]);
                    direction += i;
                    break;
                }
            }
        }

        // If someone is nearby
        else {
            if (Math.abs(x) > 2 * Math.abs(y)) {
                y = 0;
            } else if (Math.abs(y) > 2 * Math.abs(x)) {
                x = 0;
            }
            for (Direction dir : directions) {
                if (dir.getDeltaX() == x && dir.getDeltaX() == y) {
                    System.out.println("Moving to " + dir);
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                    }
                    return;
                }
            }
            System.out.println("Cannot Move!!!");
        }
    }

    static int generateRandomDirection(int i, int j) {
        return (((i % j) + j) % j);
    }

}