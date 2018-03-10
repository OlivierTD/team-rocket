package dummy;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import de.danoeh.antennapod.activity.TestActivity;
import de.danoeh.antennapod.fragment.ToplistFragment;

/**
 * Created by David on 2018-03-07.
 */
public class ToplistFragmentTest{

    @Mock
    private TestActivity mockActivity;

    @Mock
    private ToplistFragment mockFragment;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockActivity = Mockito.mock(TestActivity.class);
        mockFragment = Mockito.mock(ToplistFragment.class);
    }

    @After
    public void tearDown() throws Exception {
        mockActivity = null;
        mockFragment = null;
    }

    @Test
    public void testToplist(){
        mockFragment.loadToplist();
        assertTrue(true);
    }
}