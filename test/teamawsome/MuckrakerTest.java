package teamawsome;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MuckrakerTest {

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



}
