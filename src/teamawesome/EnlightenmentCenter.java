package teamawesome;

import battlecode.common.*;

public class EnlightenmentCenter extends GenericRobot{

    /**
     * Enlightenment Center Variables
     */
    public String robotStatement = "I'm an " + rc.getType() + "! Location " + rc.getLocation();
    protected RobotType lastBuilt;
    protected int age;
    public EnlightenmentCenter(RobotController newRc) {
        super(newRc);
        lastBuilt = null;
        age = 0;
    }

    @Override
    public void turn() throws GameActionException {
        int round = rc.getRoundNum();
        double myInf = rc.getInfluence();
        double inf;
        RobotType toBuild = strategicSpawnableRobotType(round);
        // check for nearby muckrakers, build a politician to defend
        for (RobotInfo robot:
             rc.senseNearbyRobots()) {
            if(robot.getType() == RobotType.MUCKRAKER && robot.getTeam() != rc.getTeam())
                toBuild = RobotType.POLITICIAN;
        }
        if(age <= 20) toBuild = RobotType.SLANDERER;

        if(toBuild == RobotType.POLITICIAN){
            inf = Math.pow((round *.01), 2) + 50;
            if(myInf < inf) inf = 50;
        }
        else if(toBuild == RobotType.SLANDERER){
            inf = Math.pow((round *.01), 2) + 80;
            if(myInf < inf) inf = 50;
        }else{
            inf = 20;
            // prevent getting stuck just building muckrakers
            if(lastBuilt == RobotType.MUCKRAKER) toBuild = RobotType.SLANDERER;
        }

        for (Direction dir : teamawesome.RobotPlayer.directions) {

            if (rc.canBuildRobot(toBuild, dir, (int) inf)) {
                    System.out.println("Building a " + toBuild + " in the " + dir + " direction with " + inf + " influence!");
                    lastBuilt = toBuild;
                    rc.buildRobot(toBuild, dir, (int) inf);
                }
        }


        //sense enemy robots
        int conviction = 0;
        int typeFlag = 25;
        if(rc.canSenseRobot(rc.getID())){
            RobotInfo sense = rc.senseRobot(rc.getID());

            //check team
            if(sense.team != rc.getTeam()){
                conviction = sense.conviction + 30;
                switch (sense.type) {
                    case ENLIGHTENMENT_CENTER: typeFlag = 50;   break;
                    case POLITICIAN:           typeFlag = 0;    break;
                    case SLANDERER:            typeFlag = 10;   break;
                    case MUCKRAKER:            typeFlag = 20;   break;
                }
            }

        }
        //set flag
        int flag = typeFlag + conviction;
        if(rc.canSetFlag(flag)){
            rc.setFlag(flag);
        }

        //Check the bidding conditions.
        double toBid = Math.pow((round *.01), 2);
        if(rc.canBid((int)toBid) && age >= 50){
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
        if (round < 300){
            if(round % 10 == 0){
                return(RobotType.MUCKRAKER);
            }
            else return(RobotType.SLANDERER);
        }
        else if(round > 700 && round < 900 ){
            return(RobotType.POLITICIAN);
        }
        else return teamawesome.RobotPlayer.spawnableRobot[(int) (Math.random()
                * teamawesome.RobotPlayer.spawnableRobot.length)];
    }
}
