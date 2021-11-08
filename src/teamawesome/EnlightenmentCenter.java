package teamawesome;

import battlecode.common.*;

public class EnlightenmentCenter extends GenericRobot{

    /**
     * Enlightenment Center Variables
     */
    public String robotStatement = "I'm an " + rc.getType() + "! Location " + rc.getLocation();
    protected static final int RUSH = 300;

    public EnlightenmentCenter(RobotController newRc) {
        super(newRc);
    }

    @Override
    public void turn() throws GameActionException {
        RobotType toBuild;
        // build only slanderers for the first SLAN_RUSH rounds
        if(rc.getRoundNum() < RUSH) {
            if(rc.getRoundNum() % 2 == 0) {
                toBuild = RobotType.SLANDERER;
            } else {
                toBuild = RobotType.POLITICIAN;
            }
        } else {
            toBuild = randomSpawnableRobotType();
        }
        int influence = 50;
        for (Direction dir : teamawesome.RobotPlayer.directions) {
            if (rc.canBuildRobot(toBuild, dir, influence)) {
                System.out.println("Building a " + toBuild + " in the " + dir + " direction!");
                rc.buildRobot(toBuild, dir, influence);
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
        int round = rc.getRoundNum();
        int myInfluence = rc.getInfluence();
        double toBid = (Math.pow((round - 0.7), 5) + Math.pow((round - 0.2), 3) + 0.2) / 1E+10;
        System.out.println("I would like to bid " + toBid);
        if(rc.canBid((int)toBid)){
            rc.bid((int)toBid);
            System.out.println("And I did!");
        } else if((int)toBid > myInfluence) {
            System.out.println("But I only have " + myInfluence);
            if(round % 10 == 0) {
                rc.bid(myInfluence / 9);
            }
        }
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
}
