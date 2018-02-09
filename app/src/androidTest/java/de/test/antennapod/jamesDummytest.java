package de.test.antennapod;

/**
 * Created by James on 2018-02-08.
 */

import android.test.InstrumentationTestRunner;
import android.test.suitebuilder.TestSuiteBuilder;
import junit.framework.TestSuite;


public class jamesDummytest  extends InstrumentationTestRunner {

    @Override
    public TestSuite getAllTests() {
        return new TestSuiteBuilder(AntennaPodTestRunner.class)
                .includeAllPackagesUnderHere()
                .excludePackages("de.test.antennapod.gpodnet")
                .build();
    }
    
}
