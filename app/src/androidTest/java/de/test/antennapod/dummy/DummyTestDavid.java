package de.test.antennapod.dummy;

import android.test.InstrumentationTestCase;

/**
 * Created by David on 2018-02-11.
 */

public class DummyTestDavid extends InstrumentationTestCase {

    public void test(){
        int x = 1;
        int y = 2;
        int z = 3;

        assertEquals(z, x+y);
    }
}
