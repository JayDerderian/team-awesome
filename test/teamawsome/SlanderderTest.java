package teamawsome;

import battlecode.common.*;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import teamawesome.Muckraker;
import teamawesome.Politician;
import teamawesome.Slanderer;

public class SlanderderTest {
    RobotInfo enemy1 = new RobotInfo(1, Team.B, RobotType.MUCKRAKER, 1, 1, new MapLocation(20000, 20000));
    RobotInfo enemy2 = new RobotInfo(2, Team.B, RobotType.MUCKRAKER, 1, 1, new MapLocation(20000, 20000));
    RobotInfo enemy3 = new RobotInfo(3, Team.B, RobotType.MUCKRAKER, 1, 1, new MapLocation(20000, 20000));

    RobotInfo[] enemyRobotInfoArray = { enemy1, enemy2, enemy3 };
    RobotInfo[] noNearbyArray = {};

    RobotInfo neutralEC1 = new RobotInfo(4, Team.NEUTRAL, RobotType.ENLIGHTENMENT_CENTER, 0, 0, new MapLocation(20000, 20000));
    RobotInfo neutralEC2 = new RobotInfo(5, Team.NEUTRAL, RobotType.ENLIGHTENMENT_CENTER, 0, 0, new MapLocation(20100, 20100));
    RobotInfo[] neutralECRobotInfoArray = { neutralEC1 };

    RobotInfo teamBot1 = new RobotInfo(6, Team.A, RobotType.SLANDERER, 1, 1, new MapLocation(20200, 20200));
    RobotInfo teamBot2 = new RobotInfo(7, Team.A, RobotType.MUCKRAKER, 1, 1, new MapLocation(20200, 20200));
    RobotInfo teamBot3 = new RobotInfo(8, Team.A, RobotType.POLITICIAN, 1, 1, new MapLocation(20200, 20200));
    RobotInfo teamBot4 = new RobotInfo(9, Team.A, RobotType.ENLIGHTENMENT_CENTER, 1, 1, new MapLocation(20200, 20200));
    RobotInfo[] teamRobotInfoArray = {teamBot1};

    @Test
    public void ifSlandererRobotCreatedThenSlandererClassIsCalled() {
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.SLANDERER);

        Slanderer robot = new Slanderer(rc);

        assertThat(robot.robotStatement, containsString("I'm a SLANDERER"));
    }

    @Test
    public void ifSlandererRobotSensedRobotNearby() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.SLANDERER);
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.senseNearbyRobots()).thenReturn(enemyRobotInfoArray);
        when(rc.canExpose(new MapLocation(20000, 20000))).thenReturn(true);


        Slanderer robot = new Slanderer(rc);
        robot.turn();
        assertEquals(enemyRobotInfoArray[0].getTeam(), Team.B);
        assertTrue(rc.canExpose(new MapLocation(20000, 20000)));
    }

    @Test
    public void ifSlanderersDetectedEC() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.SLANDERER);
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.getLocation()).thenReturn(new MapLocation(20001, 20001));
        when(rc.senseNearbyRobots()).thenReturn(neutralECRobotInfoArray);
        when(rc.canExpose(new MapLocation(20000, 20000))).thenReturn(true);

        Slanderer robot = new Slanderer(rc);
        robot.turn();

        assertEquals(robot.nearby[0].getLocation(), neutralEC1.getLocation());
    }

    @Test
    public void ifSlandererMoveAwayFromEnemies() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.SLANDERER);
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.senseNearbyRobots()).thenReturn(enemyRobotInfoArray);
        when(rc.canExpose(new MapLocation(20000, 20000))).thenReturn(true);


        Slanderer robot = new Slanderer(rc);
        robot.turn();
        assertEquals(robot.xLean, -1);
        assertEquals(robot.yLean, -1);
    }
}
