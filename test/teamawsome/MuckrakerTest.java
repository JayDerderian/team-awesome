package teamawsome;

import battlecode.common.*;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

//import teamawesome.GenericRobot;
import teamawesome.Muckraker;

/**
 * Constants Meaning
 * 1. rc.getTeam --> A=OurTeam; B=EnemyTeam; NEUTRAL=NEC
 * 2. new RobotInfo(int ID, Team team, RobotType type, int influence, int conviction, MapLocation location)
 * 3. new MapLocation(int x, int y)
 */
public class MuckrakerTest {

    RobotInfo enemy1 = new RobotInfo(1, Team.B, RobotType.SLANDERER, 1, 1, new MapLocation(20000, 20000));
    RobotInfo enemy2 = new RobotInfo(2, Team.B, RobotType.SLANDERER, 1, 1, new MapLocation(20000, 20000));
    RobotInfo enemy3 = new RobotInfo(3, Team.B, RobotType.SLANDERER, 1, 1, new MapLocation(20000, 20000));
    RobotInfo[] enemyRobotInfoArray = { enemy1, enemy2, enemy3 };

    RobotInfo neutralEC1 = new RobotInfo(4, Team.NEUTRAL, RobotType.ENLIGHTENMENT_CENTER, 0, 0, new MapLocation(20100, 20100));
    RobotInfo neutralEC2 = new RobotInfo(5, Team.NEUTRAL, RobotType.ENLIGHTENMENT_CENTER, 0, 0, new MapLocation(20100, 20100));
    RobotInfo[] neutralECRobotInfoArray = { neutralEC1 };

    RobotInfo enemyEC1 = new RobotInfo(10, Team.B, RobotType.ENLIGHTENMENT_CENTER, 0, 0, new MapLocation(20300, 20300));
    RobotInfo enemyEC2 = new RobotInfo(11, Team.B, RobotType.ENLIGHTENMENT_CENTER, 0, 0, new MapLocation(20300, 20300));
    RobotInfo[] enemyECRobotInfoArray = { enemyEC1 };

    RobotInfo enemyMuck1 = new RobotInfo(12, Team.B, RobotType.MUCKRAKER, 0,0, new MapLocation(2400,2400));
    RobotInfo[] enemyMuckRobotInfoArray = { enemyMuck1 };

    RobotInfo teamBot1 = new RobotInfo(6, Team.A, RobotType.SLANDERER, 1, 1, new MapLocation(20200, 20200));
    RobotInfo teamBot2 = new RobotInfo(7, Team.A, RobotType.MUCKRAKER, 1, 1, new MapLocation(20200, 20200));
    RobotInfo teamBot3 = new RobotInfo(8, Team.A, RobotType.POLITICIAN, 1, 1, new MapLocation(20200, 20200));
    RobotInfo teamBot4 = new RobotInfo(9, Team.A, RobotType.ENLIGHTENMENT_CENTER, 1, 1, new MapLocation(20200, 20200));
    RobotInfo[] teamRobotInfoArray = {teamBot1};
    RobotInfo[] teamMuckInfoArray = {teamBot2, enemyEC1};
    RobotInfo[] teamMuckInfoArray1 = { teamBot2 };

    @Test
    public void ifMuckrakerRobotCreatedThenMuckrakerClassIsCalled() {
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.MUCKRAKER);

        Muckraker robot = new Muckraker(rc);

        assertThat(robot.robotStatement, containsString("I'm a MUCKRAKER"));
    }

    @Test
    public void ifEnemyRobotSensedAndCanBeExposeThenExpose() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.MUCKRAKER);
        when(rc.getLocation()).thenReturn(new MapLocation(20200, 20200));
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.senseNearbyRobots()).thenReturn(enemyRobotInfoArray);
        when(rc.canExpose(new MapLocation(20000, 20000))).thenReturn(true);

        Muckraker robot = new Muckraker(rc);
        robot.turn();

        assertEquals(enemyRobotInfoArray[0].getTeam(), Team.B);
        assertTrue(rc.canExpose(new MapLocation(20000, 20000)));
    }

    @Test
    public void ifCanMoveInPossibleDirThenStoreItAsPrevMove() throws GameActionException {
        RobotController rc = mock(RobotController.class);
//        GenericRobot gr = mock(GenericRobot.class);
        when(rc.getType()).thenReturn(RobotType.MUCKRAKER);
        when(rc.getLocation()).thenReturn(new MapLocation(20100, 20100));
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.senseNearbyRobots()).thenReturn(enemyRobotInfoArray);
        when(rc.canExpose(new MapLocation(20000, 20000))).thenReturn(true);
        MapLocation map1 = new MapLocation(20100, 20100);
        MapLocation map2 = new MapLocation(20000, 20000);

        Muckraker robot = new Muckraker(rc);
        robot.turn();

        assertEquals(enemyRobotInfoArray[0].getLocation(), map2);
        assertEquals(rc.getLocation().directionTo(enemyRobotInfoArray[0].getLocation()), Direction.SOUTHWEST);
        assertNull(robot.prevMovedDir);
    }

    @Test
    public void ifEnemyECSensedThenSetAllEnemyECVariablesAndSetCorrectFlag() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.MUCKRAKER);
        when(rc.getLocation()).thenReturn(new MapLocation(20200, 20200));
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.senseNearbyRobots()).thenReturn(enemyECRobotInfoArray);
        when(rc.canSetFlag(11400)).thenReturn(true);
        when(rc.canGetFlag(11)).thenReturn(true);
        when(rc.getFlag(11)).thenReturn(11400);

        Muckraker robot = new Muckraker(rc);
        robot.turn();

        assertTrue(robot.enemyEcFound);
        assertEquals(robot.enemyECLocation, new MapLocation(20300, 20300));
        assertTrue(robot.enemyECLocationSet);
        assertEquals(rc.getLocation().directionTo(enemyRobotInfoArray[0].getLocation()), Direction.SOUTHWEST);
        assertEquals(rc.getFlag(11), 11400);
    }

    @Test
    public void ifSensedOurTeamBotGetItsFlagAndDetermineWhichDirectionToMove() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.MUCKRAKER);
        when(rc.getLocation()).thenReturn(new MapLocation(20200, 20200));
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.senseNearbyRobots()).thenReturn(teamRobotInfoArray);
        when(rc.canGetFlag(6)).thenReturn(true);
        when(rc.getFlag(6)).thenReturn(11400);

        Muckraker robot = new Muckraker(rc);
        robot.turn();

        assertEquals(rc.getLocation().directionTo(teamRobotInfoArray[0].getLocation()), Direction.CENTER);
        assertEquals(robot.botDirectionToMove, Direction.CENTER);
        assertNull(robot.prevMovedDir);
    }

    @Test
    public void ifFellowMuckFlag11400FoundTryMoveInBotDirTillEnemyECDiscovered() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.MUCKRAKER);
        when(rc.getID()).thenReturn(101);
        when(rc.getLocation()).thenReturn(new MapLocation(20200, 20200));
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.senseNearbyRobots()).thenReturn(teamMuckInfoArray);
        when(rc.canGetFlag(6)).thenReturn(true);
        when(rc.getFlag(6)).thenReturn(11400);
        when(rc.canMove(Direction.CENTER)).thenReturn(true);

        Muckraker robot = new Muckraker(rc);
        robot.turn();

        assertEquals(rc.getLocation().directionTo(teamRobotInfoArray[0].getLocation()), Direction.CENTER);
        assertEquals(true, rc.canMove(Direction.CENTER));
        assertTrue(robot.enemyEcFound);
    }

    @Test
    public void ifEnemyMuckFoundAlertTeamWithFlag() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.MUCKRAKER);
        when(rc.getID()).thenReturn(101);
        when(rc.getLocation()).thenReturn(new MapLocation(20200, 20200));
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.senseNearbyRobots()).thenReturn(enemyMuckRobotInfoArray);

        Muckraker robot = new Muckraker(rc);
        robot.turn();

        assertEquals(robot.flagValue, 11300);
    }

}
