package teamawsome;
import teamawesome.EnlightenmentCenter;
import teamawesome.Politician;

import static org.junit.Assert.*;
import static teamawesome.FlagConstants.*;

import battlecode.common.*;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Hashtable;



public class GenericBotTest {

    // Enemy bots
    RobotInfo enemy1 = new RobotInfo(1, Team.B, RobotType.POLITICIAN, 1, 1, new MapLocation(20000, 20000));
    RobotInfo enemy2 = new RobotInfo(2, Team.B, RobotType.MUCKRAKER, 1, 1, new MapLocation(20000, 20000));
    RobotInfo enemy3 = new RobotInfo(3, Team.B, RobotType.SLANDERER, 1, 1, new MapLocation(20000, 20000));
    RobotInfo enemy4 = new RobotInfo(4, Team.B, RobotType.ENLIGHTENMENT_CENTER, 1, 1, new MapLocation(21000, 23000));
    RobotInfo[] enemyRobotInfoArray = { enemy1, enemy2, enemy3, enemy4 };

    // Neutral EC's
    RobotInfo neutralEC1 = new RobotInfo(5, Team.NEUTRAL, RobotType.ENLIGHTENMENT_CENTER, 0, 0, new MapLocation(20100, 20100));
    RobotInfo neutralEC2 = new RobotInfo(6, Team.NEUTRAL, RobotType.ENLIGHTENMENT_CENTER, 0, 0, new MapLocation(20100, 20100));
    RobotInfo[] neutralECRobotInfoArray = { neutralEC1 };

    // Team bots
    RobotInfo teamBot1 = new RobotInfo(7, Team.A, RobotType.SLANDERER, 1, 1, new MapLocation(20200, 20200));
    RobotInfo teamBot2 = new RobotInfo(8, Team.A, RobotType.MUCKRAKER, 1, 1, new MapLocation(20200, 20200));
    RobotInfo teamBot3 = new RobotInfo(9, Team.A, RobotType.POLITICIAN, 1, 1, new MapLocation(20200, 20200));
    RobotInfo teamBot4 = new RobotInfo(10, Team.A, RobotType.ENLIGHTENMENT_CENTER, 1, 1, new MapLocation(20200, 20200));
    RobotInfo[] teamRobotInfoArray = { teamBot1, teamBot2, teamBot3, teamBot4 };


    //-------------------------------------FLAG GENERATION---------------------------------------//

    /*
     @Test - flag generation and see whether we get an error or not from the method
     @Test - test each flag constant!
     */

    @Test
    public void canMakeFlagOnOwn(){
        RobotController rc = mock(RobotController.class);
        Politician testBot = new Politician(rc);
        int test = 11400;
        int testFlag = testBot.makeFlag(ENEMY_ENLIGHTENMENT_CENTER_FLAG,0);
        assertEquals(test, testFlag);
    }

//    @Test
//    public void canMakeFlagAfterSensingEnemies() throws GameActionException{
//        RobotController rc = mock(RobotController.class);
//        Politician testBot = new Politician(rc);
//        // sense an enemy politician
//        RobotInfo info = rc.senseRobot(enemy1.getID());
//        int testFlag = 11101;
//        int conv = info.getConviction();               // This line is raising a null pointer exception!!!!
//        int newFlag = testBot.makeFlag(ENEMY_POLITICIAN_FLAG, conv);
//        rc.setFlag(newFlag);
//        assertEquals(testFlag, newFlag);
//    }

    @Test
    public void canMakeFlagAfterFindingNeutralEC() throws GameActionException{
        RobotController rc = mock(RobotController.class);
        Politician testBot = new Politician(rc);
        // Sense neutral EC
        RobotInfo neutralEc = rc.senseRobot(enemy4.getID());
        int test = 111;
        int testFlag = testBot.makeFlag(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG, 0);
        assertEquals(test, testFlag);
    }


    //--------------------------------------FLAG PARSING-----------------------------------------//

    /*
    @Test - successful retrieval of flag
    @Test - successful parsing of flag against a known and expected result
    @Test - successful retrieval of location from bot who's flag we retrieved.

    all tests should pass bad info to make sure methods are catching them!
     */

    @Test
    public void canParseFlag() throws GameActionException {
        RobotController rc1 = mock(RobotController.class);
        RobotController rc2 = mock(RobotController.class);

        Politician testBot1 = new Politician(rc1);
        Politician testBot2 = new Politician(rc2);

        int baseFlag = 111;                    // "Neutral EC found!"
        int bot2ID = rc2.getID();
        rc2.setFlag(testBot2.makeFlag(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG, 0));

        Hashtable<Integer, MapLocation> res;

        RobotInfo friendlyBot = rc1.senseRobot(rc2.getID());
        int friendlyFlag = rc1.getFlag(rc2.getID());
        res = testBot1.parseFlag(friendlyBot, friendlyFlag);

        assertTrue(res.containsKey(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG));
    }


    @Test
    public void canDetectFriendlyFlagUsingRetrieveFlagMethod() throws GameActionException{
        RobotController rc1 = mock(RobotController.class);
        RobotController rc2 = mock(RobotController.class);

        Politician testBot1 = new Politician(rc1);
        EnlightenmentCenter testBot2 = new EnlightenmentCenter(rc2);

        Hashtable<Integer, MapLocation> res = new Hashtable<>();

        // set friendly's flag to something recognizable
        int baseFlag = 11400; // "Neutral EC found!"
        rc2.setFlag(testBot2.makeFlag(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG, 0));

        // attempt to retrieve friendly flag
        res = testBot1.retrieveFlag(rc2.getID());

        // if we got the friendly's location, then maybe this worked...
        MapLocation neutralECLoc = res.get(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG);
        assertTrue(res.containsKey(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG));
    }
}
