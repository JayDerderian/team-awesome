package teamawsome;

import battlecode.common.*;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
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

//    NeutralEC
//123+xx (type+location)
//1231(location)-->(x,y)?

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
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.senseNearbyRobots()).thenReturn(enemyRobotInfoArray);
        when(rc.canExpose(new MapLocation(20000, 20000))).thenReturn(true);

        Muckraker robot = new Muckraker(rc);
        robot.turn();

        assertEquals(enemyRobotInfoArray[0].getTeam(), Team.B);
        assertTrue(rc.canExpose(new MapLocation(20000, 20000)));
    }

    @Test
    public void ifNeutralECDetectedThenGetItsMapLocationAndSetFlag() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.MUCKRAKER);
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.senseNearbyRobots()).thenReturn(neutralECRobotInfoArray);
        when(rc.getID()).thenReturn(4);
        when(rc.canSetFlag(1231)).thenReturn(true);
        when(rc.getFlag(4)).thenReturn(1231);

        Muckraker robot = new Muckraker(rc);
        robot.turn();

        assertEquals(robot.neutralLocation.x, 20100 );
        assertEquals(robot.neutralLocation.y, 20100);
        assertEquals(rc.getFlag(rc.getID()), 1231);
    }

}
