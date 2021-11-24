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

/**
 * Constants Meaning
 * 1. rc.getTeam --> A=OurTeam; B=EnemyTeam; NEUTRAL=NEC
 * 2. new RobotInfo(int ID, Team team, RobotType type, int influence, int conviction, MapLocation location)
 * 3. new MapLocation(int x, int y)
 */
public class PoliticianTest {

    public static RobotInfo enemy1 = new RobotInfo(1, Team.B, RobotType.SLANDERER, 1, 1, new MapLocation(20000, 20000));
    public static RobotInfo enemy2 = new RobotInfo(2, Team.B, RobotType.SLANDERER, 1, 1, new MapLocation(20000, 20000));
    public static RobotInfo enemy3 = new RobotInfo(3, Team.B, RobotType.SLANDERER, 1, 1, new MapLocation(20000, 20000));
    public static RobotInfo enemy4 = new RobotInfo(10, Team.B, RobotType.ENLIGHTENMENT_CENTER, 1, 1, new MapLocation(20000, 20000));
    public static RobotInfo enemy5 = new RobotInfo(11, Team.B, RobotType.MUCKRAKER, 1, 1, new MapLocation(20000, 20000));
    public static RobotInfo[] enemyRobotInfoArray = { enemy1, enemy2, enemy3, enemy5 };
    public static RobotInfo[] noNearbyArray = {};
    public static RobotInfo[] enemyEC = {enemy4};

    public static RobotInfo neutralEC1 = new RobotInfo(4, Team.NEUTRAL, RobotType.ENLIGHTENMENT_CENTER, 0, 0, new MapLocation(20100, 20100));
    public static RobotInfo neutralEC2 = new RobotInfo(5, Team.NEUTRAL, RobotType.ENLIGHTENMENT_CENTER, 0, 0, new MapLocation(20100, 20100));
    public static RobotInfo[] neutralECRobotInfoArray = { neutralEC1 };

    public static RobotInfo teamBot1 = new RobotInfo(6, Team.A, RobotType.SLANDERER, 1, 1, new MapLocation(20200, 20200));
    public static RobotInfo teamBot2 = new RobotInfo(7, Team.A, RobotType.MUCKRAKER, 1, 1, new MapLocation(20200, 20200));
    public static RobotInfo teamBot3 = new RobotInfo(8, Team.A, RobotType.POLITICIAN, 1, 1, new MapLocation(20200, 20200));
    public static RobotInfo teamBot4 = new RobotInfo(9, Team.A, RobotType.ENLIGHTENMENT_CENTER, 1, 1, new MapLocation(20200, 20200));
    public static RobotInfo[] teamRobotInfoArray = {teamBot1, teamBot2, teamBot3};
    public static RobotInfo[] PoliticECTest = {teamBot4};

    @Captor
    ArgumentCaptor<Direction> dir = ArgumentCaptor.forClass(Direction.class);

//    NeutralEC
//123+xx (type+location)
//1231(location)-->(x,y)?

    @Test
    public void ifPoliticianRobotCreatedThenPoliticianClassIsCalled() {
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.POLITICIAN);

        Politician robot = new Politician(rc);

        assertThat(robot.robotStatement, containsString("I'm a POLITICIAN"));
    }

    @Test
    public void ifEnemyRobotSensedAndCanBeEmpoweredThenEmpower() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.POLITICIAN);
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.senseNearbyRobots(9, Team.B)).thenReturn(enemyRobotInfoArray);
        when(rc.canEmpower(9)).thenReturn(true);

        Politician robot = new Politician(rc);
        robot.turn();

        assertTrue(robot.empowered);
    }

    @Test
    public void ifNeutralECDetectedThenEmpower() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.POLITICIAN);
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.senseNearbyRobots(9, Team.B)).thenReturn(noNearbyArray);
        when(rc.senseNearbyRobots(9, Team.NEUTRAL)).thenReturn(neutralECRobotInfoArray);
        when(rc.canEmpower(9)).thenReturn(true);
        when(rc.canMove(any())).thenReturn(true);

        Politician robot = new Politician(rc);
        robot.turn();
        assertTrue(robot.empowered);
    }

    @Test
    public void politicianMovesIfNoEmpowerableRobots() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.POLITICIAN);
        when(rc.getTeam()).thenReturn(Team.A);
        // show no convertible units
        when(rc.senseNearbyRobots()).thenReturn(noNearbyArray);
        when(rc.senseNearbyRobots(9, Team.B)).thenReturn(noNearbyArray);
        when(rc.senseNearbyRobots(9, Team.NEUTRAL)).thenReturn(noNearbyArray);
        when(rc.canEmpower(9)).thenReturn(false);
        when(rc.getLocation()).thenReturn(new MapLocation(20200, 20200));
        when(rc.adjacentLocation(any())).thenReturn(new MapLocation(20200, 20200));
        when(rc.onTheMap(any())).thenReturn(true);
        when(rc.canMove(any())).thenReturn(true);

        Politician robot = new Politician(rc);
        robot.turn();
        assertFalse(robot.empowered);

        verify(rc).move(dir.capture());
        assertNotNull(dir.getValue());
    }

    @Test
    public void politicianFindsMotherShip() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.POLITICIAN);
        when(rc.getTeam()).thenReturn(Team.A);
        // show no convertible units
        when(rc.senseNearbyRobots()).thenReturn(PoliticECTest);
        when(rc.senseNearbyRobots(9, Team.B)).thenReturn(noNearbyArray);
        when(rc.senseNearbyRobots(9, Team.NEUTRAL)).thenReturn(noNearbyArray);
        when(rc.canEmpower(9)).thenReturn(false);
        when(rc.getLocation()).thenReturn(new MapLocation(20201, 20201));
        when(rc.adjacentLocation(any())).thenReturn(new MapLocation(20200, 20200));
        when(rc.onTheMap(any())).thenReturn(true);
        when(rc.canMove(any())).thenReturn(true);

        Politician robot = new Politician(rc);
        robot.turn();
        assertNotEquals(-1, robot.mothership);
    }

}
