package politician;
import battlecode.common.*;

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
    }
}
