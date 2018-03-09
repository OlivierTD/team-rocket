package dummy;

import org.junit.Test;
import static junit.framework.Assert.assertEquals;

/**
 * Created by David on 2018-02-11.
 */

public class DummyTestDavid {

    @Test
    public void test(){
        int x = 1;
        int y = 2;
        int z = 3;

        assertEquals(z, x+y);
    }
}

