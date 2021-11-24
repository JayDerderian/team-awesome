package teamawsome;

import battlecode.common.*;
import teamawesome.EnlightenmentCenter;
import teamawesome.Muckraker;
import teamawesome.Politician;

import org.junit.Test;
import teamawesome.RobotPlayer;

import static org.junit.Assert.*;
import static teamawesome.FlagConstants.*;

import static org.mockito.Mockito.*;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;


public class RobotPlayerTest {

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


    //--------------------------------------TEST HELPERS-----------------------------------------//
    // NOTE: These should not be called directly! They're used by the parser and retriever methods.

    @Test
    public void testDigitCounter(){
        RobotController rc = mock(RobotController.class);
        Politician testBot = new Politician(rc);
        int total = testBot.countDigis(11111);
        assertEquals(total, 5);
    }

    @Test
    public void testIsOurs(){
        RobotController rc = mock(RobotController.class);
        Politician testBot = new Politician(rc);
        assertTrue(testBot.isOurs(11400));
    }

    @Test
    public void testIsOursWithWrongKindOfFlag(){
        RobotController rc = mock(RobotController.class);
        Politician testBot = new Politician(rc);
        assertFalse(testBot.isOurs(12400));
    }


    //-------------------------------------FLAG GENERATION---------------------------------------//

    /*
     @Test - flag generation and see whether we get an error or not from the method
     @Test - test each flag constant!
     */

    @Test
    public void canMakeFlag(){
        RobotController rc = mock(RobotController.class);
        Politician testBot = new Politician(rc);
        int test = 11400;
        int testFlag = testBot.makeFlag(ENEMY_ENLIGHTENMENT_CENTER_FLAG,0);
        assertEquals(test, testFlag);
    }

    @Test
    public void canMakeFlag2(){
        RobotController rc = mock(RobotController.class);
        Politician testBot = new Politician(rc);
        int test = 11302;
        int testFlag = testBot.makeFlag(ENEMY_MUCKRAKER_NEARBY_FLAG,2);
        assertEquals(test, testFlag);
    }

    @Test
    public void encodeDecodeCoordinatesTest() {
        RobotController rc = mock(RobotController.class);
        Politician testBot = new Politician(rc);
        int x = 12300;
        int y = 32100;
        MapLocation loc = new MapLocation(x,y);
        int flag = testBot.encodeLocationInFlag(loc);
        MapLocation loc2 = testBot.decodeLocationFromFlag(flag);
        assertEquals(x, loc2.x);
        assertEquals(y, loc2.y);
    }


    //-----------------------------------LOCATION ENCODING/DECODING------------------------------//

    /*
    @Test - make sure a flag is encoded correctly
    @Test - make sure a flag is decoded correctly
     */

    @Test
    public void encodeMapCoordinates(){
        RobotController rc = mock(RobotController.class);
        Politician testBot = new Politician(rc);
        int x = 12300;
        int y = 32100;
        MapLocation loc = new MapLocation(x,y);
        int flag = testBot.encodeLocationInFlag(loc);
        assertEquals(11123321, flag);
    }

    @Test
    public void decodeMapCoordinates(){
        RobotController rc = mock(RobotController.class);
        Politician testBot = new Politician(rc);
        int flag = 11123321;
        int x = 12300;
        int y = 32100;
        MapLocation loc = testBot.decodeLocationFromFlag(flag);
        assertEquals(x, loc.x);
        assertEquals(y, loc.y);
    }

    //-------------------------------ENEMY / NEUTRAL EC DETECTION--------------------------------//

//    @Test
//    public void detectEnemies(){
//        RobotController rc = mock(RobotController.class);
//        Politician testBot = new Politician(rc);
//        when(rc.senseNearbyRobots()).thenReturn(enemyRobotInfoArray);
//        HashMap<Integer, MapLocation> res = testBot.findThreats(rc);
//        assertFalse(res.containsKey(ERROR));
//        assertTrue(res.containsKey(ENEMY_INFO));
//    }
//
//    @Test
//    public void detectNeutralEC(){
//        RobotController rc = mock(RobotController.class);
//        Politician testBot = new Politician(rc);
//        when(rc.senseNearbyRobots()).thenReturn(neutralECRobotInfoArray);
//        HashMap<Integer, MapLocation> res = testBot.findNeutralECs(rc);
//        assertFalse(res.containsKey(ERROR));
//        assertTrue(res.containsKey(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG));
//    }

    //--------------------------------------FLAG PARSING-----------------------------------------//

    /*
    @Test - successful retrieval of flag
    @Test - successful parsing of flag against a known and expected result
    @Test - successful retrieval of location from bot who's flag we retrieved.

    all tests should pass bad info to make sure methods are catching them!
     */

//    @Test
//    public void canParse3DigitFlag() throws GameActionException {
//        RobotController rc1 = mock(RobotController.class);
//        RobotController rc2 = mock(RobotController.class);
//        Politician testBot1 = new Politician(rc1);
//        Politician testBot2 = new Politician(rc2);
//        RobotInfo info = new RobotInfo(1, Team.B, RobotType.POLITICIAN, 1, 1, new MapLocation(20000, 20000));
//
//        when(rc1.senseRobot(rc2.getID())).thenReturn(info);
//
//        int baseFlag = 111;  // "Neutral EC found!"
//        int newFlag = testBot2.makeFlag(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG, 0);
//        System.out.println("canParse3DigitFlag -> new flag = " + newFlag);
//        // sanity checks
//        assertEquals(baseFlag, newFlag);
//        rc2.setFlag(newFlag);
//
//        // sense bot 2's info and get their flag
//        RobotInfo friendlyBot = rc1.senseRobot(rc2.getID());
//        int friendlyFlag = rc1.getFlag(rc2.getID());
//        System.out.println("canParse3DigitFlag -> rc2's flag = " + friendlyFlag);
//        HashMap<Integer, MapLocation> res = testBot1.parseFlag(friendlyBot, friendlyFlag);    //Getting a null pointer exception here!!
//
//        assertFalse(res.containsKey(ERROR));
//        assertTrue(res.containsKey(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG));
//    }
//
//    @Test
//    public void canParse5DigitFlag() throws GameActionException {
//        RobotController rc1 = mock(RobotController.class);
//        RobotController rc2 = mock(RobotController.class);
//        Politician testBot1 = new Politician(rc1);
//        Politician testBot2 = new Politician(rc2);
//
//        int baseFlag = 11404;  // "Enemy EC found!"
//        rc2.setFlag(testBot2.makeFlag(ENEMY_ENLIGHTENMENT_CENTER_FLAG, 0));
//        // sanity check
//        if(rc1.getFlag(rc2.getID()) != baseFlag)
//            assertNotEquals(rc1.getFlag(rc2.getID()), baseFlag);
//
//        // sense bot 2's info and get their flag
//        RobotInfo friendlyBot = rc1.senseRobot(rc2.getID());
//        int friendlyFlag = rc1.getFlag(rc2.getID());
//        HashMap<Integer, MapLocation> res = testBot1.parseFlag(friendlyBot, friendlyFlag);    //Getting a null pointer exception here!!
//        assertTrue(res.containsKey(ENEMY_ENLIGHTENMENT_CENTER_FLAG));
//    }

//    @Test
//    public void canRetrieve() throws GameActionException{
//        RobotController rc1 = mock(RobotController.class);
//        RobotController rc2 = mock(RobotController.class);
//        Politician testBot1 = new Politician(rc1);
//        Politician testBot2 = new Politician(rc2);
//
//        int baseFlag = 11209;  // "Enemy EC found!"
//        rc2.setFlag(testBot2.makeFlag(ENEMY_SLANDERER_NEARBY_FLAG, 9));
//        // sanity check
//        if(rc1.getFlag(rc2.getID()) != baseFlag)
//            assertNotEquals(rc1.getFlag(rc2.getID()), baseFlag);
//
//        // sense bot 2's info and get their flag
//        HashMap<Integer, MapLocation> res = testBot1.retrieveFlag(rc1, rc2.getID());
//        if(res.containsKey(ERROR))
//            assertTrue(res.containsKey(ERROR));
//        assertTrue(res.containsKey(ENEMY_SLANDERER_NEARBY_FLAG));
//    }
}
