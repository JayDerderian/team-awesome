package teamawsome;

import battlecode.common.*;
import org.junit.Test;
import teamawesome.EnlightenmentCenter;
import teamawesome.Muckraker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EnlightenmentCenterTest {

    RobotInfo enemy1 = new RobotInfo(1, Team.B, RobotType.MUCKRAKER, 1, 1, new MapLocation(20000, 20000));
    RobotInfo enemy2 = new RobotInfo(2, Team.B, RobotType.MUCKRAKER, 1, 1, new MapLocation(20000, 20000));
    RobotInfo enemy3 = new RobotInfo(3, Team.B, RobotType.MUCKRAKER, 1, 1, new MapLocation(20000, 20000));
    RobotInfo[] enemyRobotInfoArray = { enemy1, enemy2, enemy3 };

    RobotInfo pEnemy1 = new RobotInfo(1, Team.B, RobotType.POLITICIAN, 1, 1, new MapLocation(20000, 20000));
    RobotInfo pEnemy2 = new RobotInfo(2, Team.B, RobotType.POLITICIAN, 1, 1, new MapLocation(20000, 20000));
    RobotInfo pEnemy3 = new RobotInfo(3, Team.B, RobotType.POLITICIAN, 1, 1, new MapLocation(20000, 20000));
    RobotInfo[] pEnemyRobotInfoArray = { pEnemy1, pEnemy2, pEnemy3 };

    @Test
    public void ifEnlightenmentCenterCreatedThenECClassIsCalled() {
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.ENLIGHTENMENT_CENTER);

        EnlightenmentCenter robot = new EnlightenmentCenter(rc);

        assertThat(robot.robotStatement, containsString("I'm an ENLIGHTENMENT_CENTER"));
    }

    /**
     * When an enemy bot is detected, base conviction = 30.
     * If bot is a Muckracker, then conviction is set to ++20.
     * Final flag number should be = 50 for Enemy Muckracker Detection.
     * @throws GameActionException
     */
    @Test
    public void ifEnemyMuckrackerDetectedSetFlag() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.ENLIGHTENMENT_CENTER);
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.senseNearbyRobots()).thenReturn(enemyRobotInfoArray);
        when(rc.getID()).thenReturn(4);
        when(rc.canGetFlag(rc.getID())).thenReturn(true);

        //when(rc.getFlag(rc.getID())).thenReturn(11300);

        EnlightenmentCenter center = new EnlightenmentCenter(rc);
        center.turn();


        //System.out.println(rc.getID());
        System.out.println(rc.getFlag(rc.getID()));
        //assertEquals(rc.getFlag(rc.getID()), 50);
    }
//
//    /**
//     * When an enemy bot is detected, base conviction = 30.
//     * If bot is a Muckracker, then conviction is set to ++20.
//     * Final flag number should be = 50 for Enemy Muckracker Detection.
//     * @throws GameActionException
//     */
//    @Test
//    public void ifEnemyPoliticianDetectedSetFlag() throws GameActionException {
//        RobotController rc = mock(RobotController.class);
//        when(rc.getType()).thenReturn(RobotType.ENLIGHTENMENT_CENTER);
//        when(rc.getTeam()).thenReturn(Team.A);
//        when(rc.senseNearbyRobots()).thenReturn(pEnemyRobotInfoArray);
//        when(rc.getID()).thenReturn(4);
//        when(rc.getFlag(rc.getID())).thenReturn(11300);
//
//        EnlightenmentCenter center = new EnlightenmentCenter(rc);
//        center.turn();
//
//
//        System.out.println(rc.getID());
//        System.out.println(center.retrieveFlag(rc, 4));
//        //assertEquals(rc.getFlag(rc.getID()), 50);
//    }

    /**
     * If the round is between 500 and 900, robot creation type is Politician.
     * @throws GameActionException
     */
    @Test
    public void ifMidGameCreatePolitician() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        EnlightenmentCenter center = mock(EnlightenmentCenter.class);

        when(rc.getType()).thenReturn(RobotType.ENLIGHTENMENT_CENTER);
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.senseNearbyRobots()).thenReturn(enemyRobotInfoArray);
        when(rc.getInfluence()).thenReturn(500);


        when(rc.getRoundNum()).thenReturn(701);
        when(center.getLastBuilt()).thenReturn(RobotType.POLITICIAN);
        center.turn();
        assertEquals(center.getLastBuilt(), RobotType.POLITICIAN);

    }

    /**
     * If the round is less than 300, robot creation type is Slanderer.
     * @throws GameActionException
     */
    @Test
    public void ifBegGameCreateSlanderer() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        EnlightenmentCenter center = mock(EnlightenmentCenter.class);

        when(rc.getType()).thenReturn(RobotType.ENLIGHTENMENT_CENTER);
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.senseNearbyRobots()).thenReturn(enemyRobotInfoArray);
        when(rc.getInfluence()).thenReturn(500);

        when(rc.getRoundNum()).thenReturn(1);
        when(center.getLastBuilt()).thenReturn(RobotType.SLANDERER);
        center.turn();
        assertEquals(center.getLastBuilt(), RobotType.SLANDERER);
    }

}
