package teamawesome;
import battlecode.common.*;
import static teamawesome.FlagConstants.*;

public strictfp class Muckraker extends GenericRobot {
    public String robotStatement = "I'm a " + rc.getType() + "! Location " + rc.getLocation();
    public boolean exposedSuccess = false;
    public Muckraker(RobotController newRc) {
        super(newRc);
    }

    /**
     * created Muckraker
     * 1. sense every Robot --> If enemy
     *                              slanderer --> then expose.
     *                              EC -->  then set an 'Enemy EC' flag
     *                              Muckraker / politician --> do nothing
     *                      --> If Neutral EC --> set 'Neutral EC' Flag
     *
     * 2. Did not sense Robot/ already Exposed enemies --> then Detect
     *      detect surrounding. a. Found some robot --> Move in that direction
     *                          b. No Robot found --> choose Random Direction with low passability.
     */
    public void turn() throws GameActionException {
//         System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
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
            // NOT ENEMY
            else if(robot.getTeam() != enemy){
                if(robot.getTeam()!= rc.getTeam() && robot.getType() == RobotType.ENLIGHTENMENT_CENTER){ // can sense Neutral EC
                    // If Neutral EC nearby, get its location and set flag.
                    robot.getLocation();
                    rc.setFlag(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG);
                }
                else if (rc.canGetFlag(robot.getID())){
                    // If Same Team Robots, then get Flag and do appropriate action.
                    int flagSensed = rc.getFlag(robot.getID()); // what info flag is telling

                    if(flagSensed == NEUTRAL_ENLIGHTENMENT_CENTER_FLAG){
                        // if Neutral EC nearby sensed robot, set the flag to same value.
                        rc.setFlag(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG);
                    }
                    else if(flagSensed == ENEMY_SLANDERER_NEARBY_FLAG){
                        // if enemy slanderer, nearby sensed robot, retrieve direction/ location from that flag
                        // set Direction_of_Muckraker to that detected value
                        DetectEnemySlanderer = true;
                    }
                }
            }
        }

        // 2. Move in Random and explore map (or) if Direction of slanderer detected, then move in that direction.
        if(DetectEnemySlanderer){
             tryMove(detectedDirection);
        } else if (tryMove(randomDirection())){
            System.out.println("I moved!");
        }
    }

}
