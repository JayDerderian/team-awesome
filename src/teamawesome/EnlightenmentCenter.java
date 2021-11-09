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
    public void turn() throws GameActionException {
        RobotType toBuild = randomSpawnableRobotType();
        int round = rc.getRoundNum();
        double inf = Math.pow((round *.01), 2) + 1;
        for (Direction dir : teamawesome.RobotPlayer.directions) {
            if (rc.canBuildRobot(toBuild, dir, (int)inf)) {
                System.out.println("Building a " + toBuild + " in the " + dir + " direction with " + inf + " influence!");
                rc.buildRobot(toBuild, dir, (int)inf);
            }
        }

        //test

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
<<<<<<< HEAD
        double toBid = Math.pow((round *.03), 2)/20;
        if(round % 20 == 0){
            if(rc.canBid((int)toBid)){
                System.out.println("Round " + round + " bidding " + toBid);
                rc.bid((int)toBid);
            }
        }

=======
        double toBid = Math.pow((round - 0.7), 5) + Math.pow((round - 0.2), 3) + 0.2;
        if(rc.canBid((int)toBid))
            rc.bid((int)toBid);
>>>>>>> 0d68b53cf36205df9d3db92936060f724d8af18a
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
