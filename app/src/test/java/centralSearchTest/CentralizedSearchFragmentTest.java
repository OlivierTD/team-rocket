package centralSearchTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import de.danoeh.antennapod.adapter.itunes.ItunesAdapter;
import de.danoeh.antennapod.fragment.CentralizedSearchFragment;
import de.danoeh.antennapod.fragment.ToplistFragment;
import de.mfietz.fyydlin.FyydClient;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by David on 2018-03-15.
 */
public class CentralizedSearchFragmentTest {

    private CentralizedSearchFragment searchFragment;
    private ItunesAdapter mockSearchAdapter;
    private FyydClient mockFYYDClient;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        searchFragment = new CentralizedSearchFragment();

        //mock dependency
        mockSearchAdapter = mock(ItunesAdapter.class);
        mockFYYDClient = mock(FyydClient.class);

        searchFragment.setSearchAdapter(mockSearchAdapter);
        searchFragment.setFYYDClient(mockFYYDClient);
        
    }

    @After
    public void tearDown() throws Exception {
        searchFragment = null;
    }

    @Test
    public void testSearch() throws Exception {
        String empty = "";
        String query = "Joe Rogan";

        searchFragment.search(empty);
        verify(searchFragment, times(1)).search("");
        assertEquals(0, searchFragment.getSearchRestultSize());

    }
}
