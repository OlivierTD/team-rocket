package dummy;
import org.junit.Test;
import static junit.framework.Assert.assertEquals;

/**
 * Created by Anania on 2/12/2018.
 */

public class DummyTestAnania {

    @Test
    public void testAnania() {

        int y = 0;
        for (int x = 0; x < 3;x++) {
            y = x;
        }
        assertEquals(2, y);
    }
}
