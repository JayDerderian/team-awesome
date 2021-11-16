package examplefuncsplayer;

import static org.junit.Assert.*;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.instanceOf;
import battlecode.common.*;

public class RobotPlayerTest {
	static final RobotType[] ROBOT_TYPES = {
			RobotType.POLITICIAN,
			RobotType.SLANDERER,
			RobotType.MUCKRAKER,
	};

	@Test
	public void testSanity() {
		assertEquals(2, 1+1);
	}

	@Test
	public void test1() throws GameActionException {
		RobotPlayer rp = new RobotPlayer();
		RobotController rc = mock(RobotController.class);
		when(rc.getType()).thenReturn(RobotType.SLANDERER);
	}



}
