package teamawsome;

import battlecode.common.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import teamawesome.GenericRobot;
import teamawesome.Muckraker;
import teamawesome.RobotPlayer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

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
    public void testSanity() {
        assertEquals(2, 1+1);
    }

    @Test
    public void test1() throws GameActionException {
        RobotPlayer rp = new RobotPlayer();
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.MUCKRAKER); // stubbing = assigning a value
    }

    @Test
    public void ifMuckrakerRobotCreatedThenMuckrakerClassIsCalled() throws GameActionException {
        RobotPlayer rp = new RobotPlayer();
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.MUCKRAKER);
//        GenericRobot robot = mock(GenericRobot.class);
//        when(robot).thenReturn(new Muckraker(rc));
        GenericRobot robot = new Muckraker(rc);
//        assertEquals("I'm a Muckraker", ((Muckraker) robot).robotStatement);
        assertThat(((Muckraker) robot).robotStatement, containsString("I'm a MUCKRAKER"));
    }

    @Test
    public void ifEnemyRobotSensedAndCanBeExposeThenExpose() throws GameActionException {
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.MUCKRAKER);
        GenericRobot robot = new Muckraker(rc);
        when(rc.getTeam()).thenReturn(Team.A); // this robots team=A
//        when(rc.getTeam().opponent()).thenReturn(Team.B); // opponent=B
        when(rc.senseNearbyRobots()).thenReturn(enemyRobotInfoArray);
        when(rc.canExpose(new MapLocation(20000, 20000))).thenReturn(true);
        ((Muckraker) robot).turn();
        assertEquals(enemyRobotInfoArray[0].getTeam(), Team.B);
        assertTrue(rc.canExpose(new MapLocation(20000, 20000)));
    }

}
