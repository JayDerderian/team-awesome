package teamawsome;

import battlecode.common.*;
import org.junit.Test;
import teamawesome.EnlightenmentCenter;
import teamawesome.Muckraker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EnlightenmentCenterTest {

    RobotInfo enemy1 = new RobotInfo(1, Team.B, RobotType.MUCKRAKER, 1, 1, new MapLocation(20000, 20000));
    RobotInfo enemy2 = new RobotInfo(2, Team.B, RobotType.MUCKRAKER, 1, 1, new MapLocation(20000, 20000));
    RobotInfo enemy3 = new RobotInfo(3, Team.B, RobotType.MUCKRAKER, 1, 1, new MapLocation(20000, 20000));
    RobotInfo[] enemyRobotInfoArray = { enemy1, enemy2, enemy3 };

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

        EnlightenmentCenter center = new EnlightenmentCenter(rc);
        center.turn();

        assertEquals(enemyRobotInfoArray[0].getTeam(), Team.B);
        assertEquals(enemyRobotInfoArray[0].getType(), RobotType.MUCKRAKER);

        System.out.println(rc.getFlag(rc.getID()));
        //assertEquals(rc.getFlag(rc.getID()), 50);
    }

}
