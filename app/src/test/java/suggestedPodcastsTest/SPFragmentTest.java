package suggestedPodcastsTest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import de.danoeh.antennapod.adapter.itunes.ItunesAdapter;
import de.danoeh.antennapod.fragment.suggestedPodcastsFragment;

/**
 * Created by Sai Shan on 2018-03-15.
 */

public class SPFragmentTest {

    //Mock values for onCreateView
    @Mock
    LayoutInflater mockInflater;
    @Mock
    ViewGroup mockViewGroup;
    @Mock
    Bundle mockBundle;

    @Mock
    private suggestedPodcastsFragment mockFrag;

    @Mock
    private ItunesAdapter.Podcast p1;
    @Mock
    private ItunesAdapter.Podcast p2;
    @Mock
    private ArrayList<String> keywords = new ArrayList<String>(){{
        keywords.add("league");
        keywords.add("gym");
        keywords.add("news");
    }};

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockFrag = Mockito.mock(suggestedPodcastsFragment.class);
    }

    @After
    public void tearDown() throws Exception {
        mockFrag = null;
    }

    @Test
    public void searchSuggestedPodTest(){
        suggestedPodcastsFragment mockFrag = new suggestedPodcastsFragment();

    }



}
