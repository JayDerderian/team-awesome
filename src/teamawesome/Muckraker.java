package teamawesome;
import battlecode.common.*;
import static teamawesome.FlagConstants.*;

public strictfp class Muckraker extends GenericRobot {

    /**
     * Variables of Muckraker
     */
    public String robotStatement = "I'm a " + rc.getType() + "! Location " + rc.getLocation();
    public boolean exposedSuccess = false;
    public MapLocation neutralLocation;

    /**
     * constructor
     * @param newRc - new RobotController Object
     */
    public Muckraker(RobotController newRc) {
        super(newRc);
    }

    /**
     * Muckraker logic
     * @throws GameActionException
     */
    public void turn() throws GameActionException {
        System.out.println(robotStatement);

        Team enemy = rc.getTeam().opponent();
        boolean DetectEnemySlanderer = false;
        Direction detectedDirection = Direction.CENTER; // random value, change later

        // 1. Sense Every Robot (max actionRadiusSquared)
        for (RobotInfo robot : rc.senseNearbyRobots()) {
            exposedSuccess = false;
            // ENEMY
            if(robot.getTeam() == enemy){
                if (robot.type.canBeExposed()) {
                    // It's a slanderer... go get them!
                    if (rc.canExpose(robot.location)) {
                        exposedSuccess = true;
                        System.out.println("e x p o s e d");
                        rc.expose(robot.location);
                        return;
                    }
                }
            }
            // NEUTRAL EC
            if(robot.getTeam() == Team.NEUTRAL) {
                neutralLocation = robot.getLocation();
                // generate flag and set flag
                if(rc.canSetFlag(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG))
                    rc.setFlag(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG);
            }
            // OUR TEAM ROBOT
            else if(robot.getTeam() != enemy) {
                if (rc.canGetFlag(robot.getID())){
                    // Get the Flag
                    int flagSensed = rc.getFlag(robot.getID());
                    // Decode the Flag
                    // yet to implement...
                    // Flag about Neutral EC
                    if(flagSensed == NEUTRAL_ENLIGHTENMENT_CENTER_FLAG){
                        rc.setFlag(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG);
                    }
                    // Flag about Enemy Slanderer
                    else if(flagSensed == ENEMY_SLANDERER_NEARBY_FLAG){
                        DetectEnemySlanderer = true;
                        // decode their location
                        // set own flag value
                    }
                }
            }
        }

        // 2. Muckraker move --> toward Enemy slanderer (or) random direction
        if(DetectEnemySlanderer){
             tryMove(detectedDirection);
        } else if (tryMove(randomDirection())){
            System.out.println("I moved!");
        }
    }
}
