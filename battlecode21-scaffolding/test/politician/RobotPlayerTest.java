package politician;

import static org.junit.Assert.*;
import org.junit.Test;
import teamawesome.Politician;

public class RobotPlayerTest {
    @Test
    public void testCanCreatePolitician() {
        Politician test = new Politician(null);
        assertNotNull(test);
    }
}
