package teamawsome;

import battlecode.common.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
    RobotInfo[] enemyRobotInfoArray = {enemy1, enemy2, enemy3, enemy4};

    // Neutral EC's
    RobotInfo neutralEC1 = new RobotInfo(5, Team.NEUTRAL, RobotType.ENLIGHTENMENT_CENTER, 0, 0, new MapLocation(20100, 20100));
    RobotInfo neutralEC2 = new RobotInfo(6, Team.NEUTRAL, RobotType.ENLIGHTENMENT_CENTER, 0, 0, new MapLocation(20100, 20100));
    RobotInfo[] neutralECRobotInfoArray = {neutralEC1};

    // Team bots
    RobotInfo teamBot1 = new RobotInfo(7, Team.A, RobotType.SLANDERER, 1, 1, new MapLocation(20200, 20200));
    RobotInfo teamBot2 = new RobotInfo(8, Team.A, RobotType.MUCKRAKER, 1, 1, new MapLocation(20200, 20200));
    RobotInfo teamBot3 = new RobotInfo(9, Team.A, RobotType.POLITICIAN, 1, 1, new MapLocation(20200, 20200));
    RobotInfo teamBot4 = new RobotInfo(10, Team.A, RobotType.ENLIGHTENMENT_CENTER, 1, 1, new MapLocation(20200, 20200));
    RobotInfo[] teamRobotInfoArray = {teamBot1, teamBot2, teamBot3, teamBot4};


    //--------------------------------------TEST HELPERS-----------------------------------------//
    // NOTE: These should not be called directly! They're used by the parser and retriever methods.

    @Test
    public void testDigitCounter() {
        RobotController rc = mock(RobotController.class);
        Politician testBot = new Politician(rc);
        int total = testBot.countDigis(11111);
        assertEquals(total, 5);
    }

    @Test
    public void testIsOurs() {
        RobotController rc = mock(RobotController.class);
        Politician testBot = new Politician(rc);
        assertTrue(testBot.isOurs(11400));
    }

    @Test
    public void testIsOursWithWrongKindOfFlag() {
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
    public void canMakeFlag() {
        RobotController rc = mock(RobotController.class);
        Politician testBot = new Politician(rc);
        int test = 11400;
        int testFlag = testBot.makeFlag(ENEMY_ENLIGHTENMENT_CENTER_FLAG, 0);
        assertEquals(test, testFlag);
    }

    @Test
    public void canMakeFlag2() {
        RobotController rc = mock(RobotController.class);
        Politician testBot = new Politician(rc);
        int test = 11302;
        int testFlag = testBot.makeFlag(ENEMY_MUCKRAKER_NEARBY_FLAG, 2);
        assertEquals(test, testFlag);
    }

    @Test
    public void encodeDecodeCoordinatesTest() {
        RobotController rc = mock(RobotController.class);
        Politician testBot = new Politician(rc);
        int x = 12300;
        int y = 32100;
        MapLocation loc = new MapLocation(x, y);
        int flag = testBot.encodeLocationInFlag(loc);
        MapLocation loc2 = testBot.decodeLocationFromFlag(flag);
        assertEquals(x, loc2.x);
        assertEquals(y, loc2.y);
    }

    //-----------------------------------------FLAG PARSING--------------------------------------//

    /*
    @Test - successful alert flag parse
    @Test - successful enemy info flag parse
    @Test - successful approx location coord parse
     */

    @Test
    public void canParse3DigitFlag() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        Politician testBot = new Politician(rc);
        int testFlag = 111;       // "Neutral EC found!"
        HashMap<Integer, MapLocation> result = testBot.parseFlag(neutralEC1,testFlag);
        assertTrue(result.containsKey(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG));
        assertEquals(5, neutralEC1.ID);
    }

    @Test
    public void canParse5DigitFlag() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        Politician testBot = new Politician(rc);
        int testFlag = 11205;       // "Enemy Slanderer with 05 conviction"
        HashMap<Integer, MapLocation> result = testBot.parseFlag(neutralEC1,testFlag);
        assertTrue(result.containsKey(ENEMY_SLANDERER_NEARBY_FLAG));
        assertEquals(5,neutralEC1.ID);
    }

    @Test
    public void canParse8DigitFlag() throws GameActionException{
        RobotController rc = mock(RobotController.class);
        Politician testBot = new Politician(rc);
        HashMap<Integer, MapLocation> result = testBot.parseFlag(neutralEC1,11201201);
        assertTrue(result.containsKey(LOCATION_INFO));
        MapLocation loc = result.get(LOCATION_INFO);
        assertEquals(20100,loc.x);
        assertEquals(20100,loc.x);
    }


    //-----------------------------------LOCATION ENCODING/DECODING------------------------------//

    /*
    @Test - make sure a flag is encoded correctly
    @Test - make sure a flag is decoded correctly
     */

    @Test
    public void encodeMapCoordinates() {
        RobotController rc = mock(RobotController.class);
        Politician testBot = new Politician(rc);
        int x = 12300;
        int y = 32100;
        MapLocation loc = new MapLocation(x, y);
        int flag = testBot.encodeLocationInFlag(loc);
        assertEquals(11123321, flag);
    }

    @Test
    public void decodeMapCoordinates() {
        RobotController rc = mock(RobotController.class);
        Politician testBot = new Politician(rc);
        int flag = 11123321;
        int x = 12300;
        int y = 32100;
        MapLocation loc = testBot.decodeLocationFromFlag(flag);
        assertEquals(x, loc.x);
        assertEquals(y, loc.y);
    }


    //--------------------------------------COMMUNICATION-----------------------------------------//


    /*
    Communications tests
     */
    @Captor
    ArgumentCaptor<Integer> flag1 = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Integer> flag2 = ArgumentCaptor.forClass(Integer.class);

    private RobotController getTxTestMuckraker() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.MUCKRAKER);
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.senseNearbyRobots()).thenReturn(PoliticianTest.enemyEC);
        when(rc.getLocation()).thenReturn(new MapLocation(20201, 20201));
        when(rc.adjacentLocation(any())).thenReturn(new MapLocation(20200, 20200));
        when(rc.onTheMap(any())).thenReturn(true);
        when(rc.canMove(any())).thenReturn(true);
        when(rc.canSetFlag(111)).thenReturn(true);
        when(rc.canSetFlag(11200200)).thenReturn(true);
        return rc;
    }

    @Test
    public void canTransmitLocation() throws GameActionException {
        // first create a muckraker rc
        RobotController rc = getTxTestMuckraker();

        // instantiate and activate a muckraker using the mock controller
        Muckraker muck = new Muckraker(rc);
        muck.turn();

        // verify the muckraker set its flag
        verify(rc).setFlag(flag1.capture());
        assertEquals((long) flag1.getValue(), 111);

        // actuate another turn and check location is set correctly
        muck.turn();
        verify(rc, times(2)).setFlag(flag2.capture());
        for (Integer i :
                flag2.getAllValues()) {
            if (i == 11200200) {
                return;
            }
        }
        fail("correct flag not found");
    }

    @Test
    public void canReceiveLocation() throws GameActionException {
        Map<Integer, MapLocation> flag = new HashMap<>();
        flag.put(NEUTRAL_ENLIGHTENMENT_CENTER_FLAG, null);
        // instantiate an EC
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.ENLIGHTENMENT_CENTER);
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.senseNearbyRobots()).thenReturn(enemyRobotInfoArray);
        when(rc.getInfluence()).thenReturn(500);
        when(rc.getRoundNum()).thenReturn(350);
        when(rc.senseNearbyRobots()).thenReturn(PoliticianTest.teamRobotInfoArray);
        when(rc.canSetFlag(111)).thenReturn(true);
        when(rc.canSetFlag(11111111)).thenReturn(true);
        when(rc.canGetFlag(7)).thenReturn(true);
        when(rc.getFlag(7)).thenReturn(111);
        EnlightenmentCenter center = new EnlightenmentCenter(rc);
        center.testMode = true;

        // execute a turn
        center.turn();

        // check that the EC has set its flag
        verify(rc).setFlag(flag1.capture());
        assertEquals((long) flag1.getValue(), 111);

        // change the mock behavior to model a real muckraker and execute another turn
        when(rc.getFlag(7)).thenReturn(11111111);
        center.turn();
        verify(rc, times(2)).setFlag(flag1.capture());
        for (Integer i :
                flag1.getAllValues()) {
            if (i == 11111111) {
                return;
            }
        }
        fail("correct flag not found");
    }
}