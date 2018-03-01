package de.test.antennapod.dummy;

import android.test.InstrumentationTestCase;

/**
 * Created by Vartan Benohanian on 2018-02-12.
 */

public class DummyTestVartan extends InstrumentationTestCase {

    public void testVartan(){

        String first = "vartan";
        String last = "beno";
        String githubID = first + last;

        assertEquals("vartanbeno", githubID);

    }

}
