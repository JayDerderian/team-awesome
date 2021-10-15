package politician;

import static org.junit.Assert.*;
import org.junit.Test;

public class RobotPlayerTest {
    @Test
    public void testCanCreatePolitician() {
        RobotPlayer test = new RobotPlayer(null);
        assertNotNull(test);
    }
}
