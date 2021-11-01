package teamawesome;

import battlecode.common.*;

public class EnlightenmentCenter extends GenericRobot{

    /**
     * Enlightenment Center Variables
     */
    public String robotStatement = "I'm an " + rc.getType() + "! Location " + rc.getLocation();

    public EnlightenmentCenter(RobotController newRc) {
        super(newRc);
    }

    @Override
    void turn() throws GameActionException {
        RobotType toBuild = randomSpawnableRobotType();
        int influence = 15;
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

            //set flag
            int flag = typeFlag + conviction;
            if(rc.canSetFlag(flag)){
                rc.setFlag(flag);
            }

        }

        //Check the bidding conditions.
        int round = rc.getRoundNum();
        double toBid = Math.pow((round - 0.7), 5) + Math.pow((round - 0.2), 3) + 0.2;
        if(rc.canBid((int)toBid)){
            rc.bid((int)toBid);
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
