package de.test.antennapod;

import android.support.test.rule.ActivityTestRule;
import android.view.View;
import android.widget.RelativeLayout;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.danoeh.antennapod.activity.TestActivity;
import de.danoeh.antennapod.fragment.ToplistFragment;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

/**
 * Created by David on 2018-03-07.
 */
public class ToplistFragmentTest{

    //test activity to run the test fragment
    @Rule
    public ActivityTestRule<TestActivity> mainActivityTestRule = new ActivityTestRule<TestActivity>(TestActivity.class);

    private TestActivity mainActivity = null;

    @Before
    public void setUp() throws Exception {
        mainActivity = mainActivityTestRule.getActivity();
    }

    @After
    public void tearDown() throws Exception {
        mainActivity = null;
        mainActivityTestRule = null;
    }

    //test if fragment launches successfully
    @Test
    public void testLaunch(){
        //call test container in test layout
        RelativeLayout container = (RelativeLayout) mainActivity.findViewById(de.danoeh.antennapod.R.id.test_container);

        //check that container is not null
        assertNotNull(container);

        ToplistFragment toplistFragmentTest = new ToplistFragment();

        //start transaction w/ test activity: load ToplistFragment in test container
        mainActivity.getSupportFragmentManager().beginTransaction().add(container.getId(), toplistFragmentTest).commitAllowingStateLoss();

        //wait for fragment to load before getting the views
        getInstrumentation().waitForIdleSync();

        View txtView = toplistFragmentTest.getView().findViewById(de.danoeh.antennapod.R.id.textTopItunes);
        View txtGrid = toplistFragmentTest.getView().findViewById(de.danoeh.antennapod.R.id.gridViewHome);

        //check if toplist title message loaded
        assertNotNull(txtView);

        //check if toplist result grid loaded
        assertNotNull(txtGrid);
    }
}