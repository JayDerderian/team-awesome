package teamawesome;

import battlecode.common.*;
import java.util.HashMap;
import java.util.Map;

import static teamawesome.FlagConstants.*;
public class EnlightenmentCenter extends RobotPlayer{

    /**
     * Enlightenment Center Variables
     */
    protected static final int BID_START = 800;
    public String robotStatement = "I'm an " + rc.getType() + "! Location " + rc.getLocation();
    protected RobotType lastBuilt;
    public EnlightenmentCenter(RobotController newRc) {
        super(newRc);
        lastBuilt = null;
    }

    @Override
    public void turn() throws GameActionException {
        int round = rc.getRoundNum();
        double myInf = rc.getInfluence();
        Team myTeam = rc.getTeam();
        double inf;
        RobotType toBuild = strategicSpawnableRobotType(round);
        // check for nearby muckrakers, build a politician to defend
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot:
             robots) {
            if(robot.getType() == RobotType.MUCKRAKER && robot.getTeam() != myTeam)
                toBuild = RobotType.POLITICIAN;
        }
        if(scoutedAge > 0 && scoutedAge < 6) toBuild = RobotType.POLITICIAN;

        if(round < 300){
            for (RobotInfo robot:
                    robots) {
                if (robot.getTeam() == myTeam) {
                    switch (robot.type) {
                        case POLITICIAN:
                            toBuild = strategicSpawnableRobotType(round);
                    }
                }
            }
        }

        if(toBuild == RobotType.POLITICIAN){
            if(round < 400){
                inf = 150;
            }else{
                inf = Math.pow((round *.01), 2) + 50;
            }
            if(myInf < inf) inf = 50;
        }
        else if(toBuild == RobotType.SLANDERER){
            inf = Math.pow((round *.01), 2) + 80;
        }else{
            inf = 1;
            // prevent getting stuck just building muckrakers
            if(lastBuilt == RobotType.MUCKRAKER) toBuild = RobotType.SLANDERER;
        }

        //if influence of home EC is high, generate muckrackers with MORE influence



        //sense enemy robots
        for (RobotInfo robot:
                robots) {
            if(robot.getTeam() != myTeam){

                switch (robot.type) {
                    case POLITICIAN:
                        rc.setFlag(makeFlag(FlagConstants.ENEMY_POLITICIAN_FLAG, 0));   break;
                    case SLANDERER:
                        rc.setFlag(makeFlag(FlagConstants.ENEMY_SLANDERER_NEARBY_FLAG, 0));   break;
                    case MUCKRAKER:
                        rc.setFlag(makeFlag(FlagConstants.ENEMY_MUCKRAKER_NEARBY_FLAG, 0));   break;
                }
            } if(robot.getTeam() == myTeam) {
                updateContact(robot);
            }

        }

        // handle comms
        if(scoutedAge > 50) {
            scouted = null;
            rxsender = -1;
            scoutedAge = 0;
        }
        if(rxsender == -1) {
            checkRolodex();
        }
        if(rxsender != -1) {
            Map<Integer, MapLocation> location = rxLocation(rxsender);
            if(location.containsKey(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG)) {
                System.out.println("Neutral EC Scouted by " + rxsender);
                scouted = location.get(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG);
            }
            txLocation(FlagConstants.NEUTRAL_ENLIGHTENMENT_CENTER_FLAG, scouted, 0);
            ++scoutedAge;
        }

        //if low influence, raise need help flag
        if(round > 100 && rc.getInfluence() < 200 && rc.getConviction() < 200){
            rc.setFlag(makeFlag(FlagConstants.NEED_HELP, 0));
        }

        //build
        for (Direction dir : teamawesome.RobotPlayer.directions) {

            if (rc.canBuildRobot(toBuild, dir, (int) inf)) {
                System.out.println("Building a " + toBuild + " in the " + dir + " direction with " + inf + " influence!");
                lastBuilt = toBuild;
                rc.buildRobot(toBuild, dir, (int) inf);
            }
        }

        //Bid
        int toBid;
        //if(round < BID_START){
            toBid = (int)Math.pow((round *.01), 2);
            if(rc.canBid((int)toBid)){
                System.out.println("Round " + round + " bidding " + toBid);
                rc.bid((int)toBid);
            }
       /* }else {
            toBid = (int)0.1 * rc.getInfluence();
            if(rc.canBid(toBid)) {
                rc.bid(toBid);
            }
        }*/
    }
    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnableRobotType() {
        return teamawesome.RobotPlayer.spawnableRobot[(int) (Math.random()
                * teamawesome.RobotPlayer.spawnableRobot.length)];
    }

    /**
     * Strategic Robot Spawn Type
     * public static final RobotType[] spawnableRobot = {
     *             RobotType.POLITICIAN,
     *             RobotType.SLANDERER,
     *             RobotType.MUCKRAKER,
     *     };
     */
    static RobotType strategicSpawnableRobotType(int round) {
        //if (round < 400){
            if(round % 5 == 0){
                return(RobotType.MUCKRAKER);
            }
            if(round %30 == 0){
                return(RobotType.POLITICIAN);
            }
            else return(RobotType.SLANDERER);
      /*  }
        else if(round > 600 && round < 800 ){
            return(RobotType.POLITICIAN);
        }
        else return teamawesome.RobotPlayer.spawnableRobot[(int) (Math.random()
                * teamawesome.RobotPlayer.spawnableRobot.length)];*/
    }

    public RobotType getLastBuilt(){
        return lastBuilt;
    }
}
