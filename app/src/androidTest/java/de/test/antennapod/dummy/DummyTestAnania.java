package de.test.antennapod.dummy;
import android.test.InstrumentationTestCase;

/**
 * Created by Anania on 2/12/2018.
 */

public class DummyTestAnania extends InstrumentationTestCase{

    public void Anania() {

        int y = 0;
        for (int x = 0; x < 3;x++) {
            y = x;
        }
        assertEquals(2, y);
    }
}
