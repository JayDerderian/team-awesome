package teamawsome;

import battlecode.common.*;
import org.junit.Test;
import teamawesome.EnlightenmentCenter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EnlightenmentCenterTest {


    @Test
    public void ifEnlightenmentCenterCreatedThenECClassIsCalled() {
        RobotController rc = mock(RobotController.class);
        when(rc.getType()).thenReturn(RobotType.ENLIGHTENMENT_CENTER);

        EnlightenmentCenter robot = new EnlightenmentCenter(rc);

        assertThat(robot.robotStatement, containsString("I'm an ENLIGHTENMENT_CENTER"));
    }

}
