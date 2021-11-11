package teamawesome;

import battlecode.common.*;

public class EnlightenmentCenter extends GenericRobot{

    /**
     * Enlightenment Center Variables
     */
    public String robotStatement = "I'm an " + rc.getType() + "! Location " + rc.getLocation();
    protected RobotType lastBuilt;
    protected int robotsBuilt;
    protected int age;
    public EnlightenmentCenter(RobotController newRc) {
        super(newRc);
        lastBuilt = null;
        age = 0;
        robotsBuilt = 0;
    }

    @Override
    public void turn() throws GameActionException {
        int round = rc.getRoundNum();
        double myInf = rc.getInfluence();
        double inf;
        RobotType toBuild = strategicSpawnableRobotType(round);
        // for first 50 rounds after creation, build the slanderer
        if(age <= 50) toBuild = RobotType.SLANDERER;
        // check for nearby muckrakers, build a politician to defend
        for (RobotInfo robot:
             rc.senseNearbyRobots()) {
            if(robot.getType() == RobotType.MUCKRAKER && robot.getTeam() != rc.getTeam())
                toBuild = RobotType.POLITICIAN;
        }

        if(toBuild == RobotType.POLITICIAN){
            inf = Math.pow((round *.01), 2) + 50;
            if(myInf < inf) inf = (int)Math.max(50, 0.10 * rc.getInfluence());
        }
        else if(toBuild == RobotType.SLANDERER){
            inf = Math.pow((round *.01), 2) + 150;
            if(myInf < inf) inf = (int)Math.max(50, 0.10 * rc.getInfluence());
        }else{
            inf = 20;
            // prevent getting stuck just building muckrakers
            if(lastBuilt == RobotType.MUCKRAKER) toBuild = RobotType.SLANDERER;
        }


        //sense enemy robots
        for (RobotInfo robot:
                rc.senseNearbyRobots()) {
            if(robot.getTeam() != rc.getTeam()){

                switch (robot.type) {
                    case POLITICIAN:
                        rc.setFlag(makeFlag(FlagConstants.ENEMY_POLITICIAN_FLAG, 0));   break;
                    case SLANDERER:
                        rc.setFlag(makeFlag(FlagConstants.ENEMY_SLANDERER_NEARBY_FLAG, 0));   break;
                    case MUCKRAKER:
                        rc.setFlag(makeFlag(FlagConstants.ENEMY_MUCKRAKER_NEARBY_FLAG, 0));   break;
                }
            }

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
                    ++robotsBuilt;
                    rc.buildRobot(toBuild, dir, (int) inf);
                }
                System.out.println("Building a " + toBuild + " in the " + dir + " direction with " + inf + " influence!");
                lastBuilt = toBuild;
                rc.buildRobot(toBuild, dir, (int) inf);
            }



        //Check the bidding conditions.
        double toBid = Math.pow((round *.01), 2);
        if(rc.canBid((int)toBid)){
            System.out.println("Round " + round + " bidding " + toBid);
            rc.bid((int)toBid);
        }
        ++age;
    }
    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnableRobotType(int round) {
        int diceRoll = (int) (Math.random() * 10);
        if(round < 600) {
            if(diceRoll == 2 || diceRoll == 4 || diceRoll == 6) {
                return RobotType.MUCKRAKER;
            }
            if(diceRoll % 2 == 0) {
                return RobotType.SLANDERER;
            }
            return RobotType.POLITICIAN;
        } else {
            if(diceRoll % 2 == 0) {
                return RobotType.SLANDERER;
            }
            return RobotType.POLITICIAN;

        }
    }

    /**
     * Strategic Robot Spawn Type
     * public static final RobotType[] spawnableRobot = {
     *             RobotType.POLITICIAN,
     *             RobotType.SLANDERER,
     *             RobotType.MUCKRAKER,
     *     };
     */
    protected RobotType strategicSpawnableRobotType(int round) {
        if (round < 300){
            if(robotsBuilt % 10 == 0){
                return(RobotType.MUCKRAKER);
            }
            else return(RobotType.SLANDERER);
        }
        else if(round > 700 && round < 900 ){
            return(RobotType.POLITICIAN);
        }
        else return randomSpawnableRobotType(round);
    }
}
