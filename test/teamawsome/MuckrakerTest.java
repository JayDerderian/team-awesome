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
    RobotInfo[] enemyRobotInfoArray = {enemy1, enemy2, enemy3};

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

}
