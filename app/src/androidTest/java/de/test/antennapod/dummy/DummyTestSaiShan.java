package de.test.antennapod.dummy;
import android.test.InstrumentationTestCase;

/**
 * Created by Sai Shan on 2018-02-12.
 */

public class DummyTestSaiShan extends InstrumentationTestCase {

    public void testSai(){

        String str = "testing";
        String s = "test";
        String t = "ing";

        s = s.concat(t);

        assertEquals(str, s);
    }
}
