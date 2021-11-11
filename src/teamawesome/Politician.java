package teamawesome;
import battlecode.common.*;
import com.sun.tools.doclint.Checker;

import java.util.*;

/**
 * Politician
 *
 * the politician robot. constructor requires a RobotController object
 * RobotPlayer should call turn() once per turn to exercise the robot
 */
public class Politician extends GenericRobot {

    LinkedList<MapLocation> history;
    HashMap<Direction, Double> momentum;
    int mothership = -1;
    int homeFlag = -1;
    public String robotStatement = "I'm a " + rc.getType() + "! Location " + rc.getLocation();
    public boolean empowered;
    LinkedList<Integer> rolodex;
    boolean juggernaut; // juggernaut Politicians will ignore all enemies and focus on neutral ECs

    public Politician(RobotController newRc) {
        super(newRc);
        setup();
        juggernaut = false;
    }

    public Politician(RobotController newRc, boolean juggernautStatus) {
        super(newRc);
        setup();
        juggernaut = juggernautStatus;
    }

    private void setup() {
        empowered = false;
        history = new LinkedList<>();
        // initialize momentum to 0 in all directions
        momentum = new HashMap<>();
        for (Direction d:
                Direction.values()) {
            if(d != Direction.CENTER) momentum.put(d, 0.0);
        }
        rolodex = new LinkedList<>();
    }

    /**
     * Politician's turn, attempts to empower enemies first, then neutral ECs
     * then moves.
     * @throws GameActionException
     */
    public void turn() throws GameActionException {
        // check mothership for flag value
        rc.setFlag(0);
        // read mothership flag
        if(mothership != -1) {
            try{
                homeFlag = rc.getFlag(mothership);
            } catch(GameActionException e) {
                // if cannot read mothership flag, the mothership EC is dead
                homeFlag = -1;
            }
        }
        System.out.println("I'm a politician! My mothership is " + mothership + " and their flag is " + homeFlag);
        checkRolodex();
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius) && !juggernaut) {
            System.out.println("empowering...");
            rc.empower(actionRadius);
            System.out.println("empowered");
            empowered = true;
            return;
        } else {
            RobotInfo[] convertable = rc.senseNearbyRobots(actionRadius, Team.NEUTRAL);
            if(convertable.length != 0 && rc.canEmpower(actionRadius)) {
                System.out.println("you will be assimilated");
                rc.empower(actionRadius);
                empowered = true;
                return;
            }
        }
        // then try to move
        Direction d = whereToMove();
        if(rc.canMove(d)) {
            // move and increment the momentum for that direction
            rc.move(d);
            System.out.println("I Moved! Direction: " + d.toString() + " | Momentum: " + momentum.get(d));
            if(momentum.containsKey(d)) momentum.put(d, momentum.get(d) + 1);
            else momentum.put(d, 1.0);
        } else {
            // if the robot can't move that direction, degrade momentum
            System.out.println("I'm Stuck! | Momentum: " + momentum.get(d));
            if(momentum.containsKey(d)) momentum.put(d, momentum.get(d) - 1);
            else momentum.put(d, 1.0);
        }
    }

    /**
     * Function for the politician to decide which direction to move
     * @return optimum direction to go
     * @throws GameActionException
     */
    public Direction whereToMove() throws GameActionException {
        // record this location in the history list
        history.add(rc.getLocation());
        if(history.size() > 25) {
            history.removeLast();
        }
        // degrade momentum values
        Set<Map.Entry<Direction, Double>> entries = momentum.entrySet();
        for (Map.Entry<Direction, Double> e:
                entries) {
            e.setValue(e.getValue() * 0.9);
        }
        // location hashmap for decision making
        HashMap<Direction, Double> locations = new HashMap<>();
        Direction toMove = null;
        // check the squares in each direction for passability
        // this forms the basis for direction weights
        for(Direction d:
                Direction.values()) {
            initWeights(locations, d);
        }
        // check nearby robots
        RobotInfo[] info = rc.senseNearbyRobots();
        // update weights of different directions depending on what robots are that way
        for (RobotInfo robot:
                info) {
            updateWeights(locations, robot);
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
            toMove = Direction.SOUTH;
        } else {
            toMove = best.getKey();
        }
        System.out.println("So I want to move " + toMove + " to space " + rc.adjacentLocation(toMove));
        return toMove;
    }

    /**
     * Function to process a nearby politician and save its ID for later reference
     * @param robot the other politician
     * @throws GameActionException because it uses the RobotController
     */
    private void checkPolitic(RobotInfo robot) throws GameActionException {
        int friendID = robot.getID();
        //retrieveFlag(rc, friendID);
        System.out.println("Found another politician! ID #" + friendID);
        if(!rolodex.contains(friendID)) rolodex.add(friendID);
    }

    private void initWeights(HashMap<Direction, Double> locations, Direction d) throws GameActionException {
        MapLocation thisLocation = rc.adjacentLocation(d);
        if(rc.onTheMap(thisLocation) && d != Direction.CENTER) {
            // if the direction is valid, set the weight to the passability of the square plus the momentum
            double thisPass = rc.sensePassability(thisLocation);
            if(momentum.containsKey(d)) thisPass += momentum.get(d);
            locations.put(d, thisPass);
        }
        // avoid map edges
        if(!rc.onTheMap(thisLocation)) {
            locations.put(d, -5.0);
        }
    }

    /**
     * Update the weights for the locations hashmap, given a robot object
     * @param locations the locations hashmap
     * @param robot a RobotInfo object
     */
    private void updateWeights(HashMap<Direction, Double> locations, RobotInfo robot) throws GameActionException {
        Direction robotDirection = rc.getLocation().directionTo(robot.getLocation());
        double dirWeight = locations.get(robotDirection);
        // prefer to go toward enemy robots
        if(robot.getTeam() != rc.getTeam()) {
            dirWeight += 1;
            if(robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                dirWeight += 1;
                // raise a flag that an EC has been found
                rc.setFlag(makeFlag(FlagConstants.NEUTRAL_ENLIGHTENMENT_CENTER_FLAG, 0));
            }
        } else {
            if(robot.getType() != RobotType.POLITICIAN)
                dirWeight -= 0.5;
            else {
                checkPolitic(robot);
                HashMap<Integer, MapLocation> flag = retrieveFlag(rc, robot.getID());
                // strongly prefer to travel toward politicians that have sighted a neutral EC
                if(flag.containsKey(FlagConstants.NEUTRAL_ENLIGHTENMENT_CENTER_FLAG)) {
                    System.out.println("An ally found a neutral EC! Location: " +
                            flag.get(FlagConstants.NEUTRAL_ENLIGHTENMENT_CENTER_FLAG));
                    dirWeight += 5;
                }

            }
            if(robot.getType() == RobotType.ENLIGHTENMENT_CENTER) mothership = robot.getID();
        }
        locations.put(robotDirection, dirWeight);
    }

    /**
     * Function to go through rolodex and check each politician
     * @throws GameActionException cause RobotController
     */
    private void checkRolodex() throws GameActionException{
        System.out.println("Here is my rolodex:");
        LinkedList<Integer> toRemove = new LinkedList<>();
        for (Integer id:
             rolodex) {
            try {
                int flag = rc.getFlag(id);
                if(rc.canSenseRobot(id)) {
                    RobotInfo friend = rc.senseRobot(id);
                    System.out.println("ID #" + id + " at location " + friend.getLocation() + " with flag " + flag);
                } else {
                    System.out.println("ID #" + id + " with flag " + flag + " cannot be sensed!");
                }
            } catch(GameActionException e) { // the ID could not be found, meaning it's time to delete that entry
                System.out.println("ID #" + id + " is dead!");
                toRemove.add(id);
            }
        }
        for (Integer del:
             toRemove) {
            rolodex.remove(del);
        }
    }
}


