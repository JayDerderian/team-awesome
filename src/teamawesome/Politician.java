package teamawesome;
import battlecode.common.*;

import java.util.*;

/**
 * Politician
 *
 * the politician robot. constructor requires a RobotController object
 * RobotPlayer should call turn() once per turn to exercise the robot
 */
public class Politician extends RobotPlayer {

    LinkedList<MapLocation> history;
    HashMap<Direction, Double> momentum;
    int homeFlag = -1;
    public String robotStatement = "I'm a " + rc.getType() + "! Location " + rc.getLocation();
    public boolean empowered;
    int foundEC; // ID of a fellow politician which found an EC
    boolean juggernaut; // juggernaut Politicians will ignore all enemies and focus on neutral ECs
    boolean ECsighted;
    int sync;
    MapLocation dest;
    int destAge;
    MapLocation myLoc;

    public Politician(RobotController newRc) {
        super(newRc);
        setup();
        juggernaut = rc.getInfluence() > 400;
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
        dest = null;
        ECsighted = false;
        foundEC = -1;
    }

    /**
     * Politician's turn, attempts to empower enemies first, then neutral ECs
     * then moves.
     * @throws GameActionException
     */
    public void turn() throws GameActionException {
        System.out.println("I'm a politician! My mothership is " + mothership + " and their flag is " + homeFlag);
        // check mothership for flag value
        hasSetFlag = false;
        myLoc = rc.getLocation();
        // read mothership flag
        if(mothership != -1) {
            try{
                Map<Integer, MapLocation> flag = rxLocation(mothership);
                if(flag != null)
                    if(flag.containsKey(FlagConstants.NEUTRAL_ENLIGHTENMENT_CENTER_FLAG)) {
                        MapLocation newDest = flag.get(FlagConstants.NEUTRAL_ENLIGHTENMENT_CENTER_FLAG);
                        checkAndGo(myLoc, newDest);
                    } else if(flag.containsKey(FlagConstants.ENEMY_ENLIGHTENMENT_CENTER_FLAG)) {
                        MapLocation newDest = flag.get(FlagConstants.ENEMY_ENLIGHTENMENT_CENTER_FLAG);
                        checkAndGo(myLoc, newDest);
                    } else if(flag.containsKey(FlagConstants.NEED_HELP)) {
                        MapLocation newDest = motherLoc;
                        juggernaut = false;
                        if(newDest.distanceSquaredTo(myLoc) > 25) {
                            dest = newDest;
                            System.out.println("Location Received: " + dest + " so I'll go " +
                                    rc.getLocation().directionTo(dest));
                        }
                    }
            } catch(GameActionException e) {
                // if cannot read mothership flag, the mothership EC is dead
                mothership = -1;
                homeFlag = -1;
            }
        }
        //checkRolodex();
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        for (RobotInfo ec:
             attackable) {
            if(ec.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                System.out.println("empowering...");
                rc.empower(actionRadius);
                System.out.println("empowered");
                empowered = true;
                return;
            }
        }
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
        if(!hasSetFlag && rc.canSetFlag(FlagConstants.NEUTRAL)) rc.setFlag(FlagConstants.NEUTRAL);
    }

    private void checkAndGo(MapLocation myLoc, MapLocation newDest) {
        if(newDest != null)
            if(!newDest.equals(myLoc)) {
                dest = newDest;
                System.out.println("Location Received: " + dest + " so I'll go " +
                        rc.getLocation().directionTo(dest));
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
        // if there is a destination, weight that heavily
        seekDest(locations);
        // check nearby robots
        RobotInfo[] info = rc.senseNearbyRobots();
        // update weights of different directions depending on what robots are that way
        for (RobotInfo robot:
                info) {
            updateWeights(locations, robot);
        }
        // prefer to go away from the way we came, also check if we're stuck
        MapLocation checkVal = history.get(0);
        boolean same = true;
        for(MapLocation l :
                history) {
            if(myLoc.directionTo(l) != Direction.CENTER) {
                Direction histDirection = rc.getLocation().directionTo(l);
                locations.put(histDirection, locations.get(histDirection) - 0.25);
            }
            // check if all locations in history are the same
            if(same) if(!myLoc.equals(checkVal)){
                same = false;
            }
        }
        if(same) {
            dest = null;
            destAge = 0;
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

    protected boolean seekDest(HashMap<Direction, Double> locations) {
        if(dest != null) {
            if(rc.getLocation().distanceSquaredTo(dest) < 25) {
                dest = null;
                return true;
            }
            locations.remove(rc.getLocation().directionTo(dest));
            locations.put(rc.getLocation().directionTo(dest), 25.0);
            return true;
        }
        return false;
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
            dirWeight += 2;
            if(robot.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                dirWeight += 15;
                int type = FlagConstants.NEUTRAL_ENLIGHTENMENT_CENTER_FLAG;
                if(robot.getTeam() == rc.getTeam().opponent()) type = FlagConstants.ENEMY_ENLIGHTENMENT_CENTER_FLAG;
                if(txLocation(type, robot.getLocation(), 0))
                    System.out.println("Message Sent!");
            }
        } else {
            if(robot.getType() != RobotType.POLITICIAN)
                dirWeight -= 0.5;
            else if(!ECsighted){
                updateContact(robot);
                int flag = poliReadFlag(rc.getFlag(robot.getID()));
                // strongly prefer to travel toward politicians that have sighted a neutral EC
                if(flag == FlagConstants.NEUTRAL_ENLIGHTENMENT_CENTER_FLAG) {
                    System.out.println("An ally found a neutral EC! Location: " +
                            robot.getLocation());
                    //dirWeight += 5;
                }
            }
            if(robot.getType() == RobotType.ENLIGHTENMENT_CENTER) mothership = robot.getID();
        }
        locations.put(robotDirection, dirWeight);
    }



    protected int poliReadFlag(int rawFlag) {
        int flag;
        int len = countDigis(rawFlag);
        flag = rawFlag % 10;
        if(flag == FlagConstants.NEUTRAL_ENLIGHTENMENT_CENTER_FLAG)
            return FlagConstants.NEUTRAL_ENLIGHTENMENT_CENTER_FLAG;
        if(flag == FlagConstants.NEED_HELP)
            return FlagConstants.NEED_HELP;
        if(flag == FlagConstants.ENEMY_ENLIGHTENMENT_CENTER_FLAG)
            return FlagConstants.ENEMY_ENLIGHTENMENT_CENTER_FLAG;
        else return FlagConstants.NEUTRAL;
    }
}


