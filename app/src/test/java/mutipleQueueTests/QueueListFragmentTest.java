package mutipleQueueTests;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import de.danoeh.antennapod.adapter.QueuesAdapter;
import de.danoeh.antennapod.core.feed.QueueObject;
import de.danoeh.antennapod.fragment.QueueListFragment;
import static junit.framework.Assert.assertEquals;


/**
 * Created by James on 2018-03-09.
 */

public class QueueListFragmentTest {

    //Class to be tested
    private QueueListFragment qlFragment;

    //Dependencies
    private QueuesAdapter mockQueueAdapter;

    @Mock
    private ArrayList<QueueObject> mockQueueList;

    @Before
    public void beforeTests(){
        MockitoAnnotations.initMocks(this);

        //Our fragment to be tested
        qlFragment = new QueueListFragment();

        //Mocking dependencies
        mockQueueAdapter = mock(QueuesAdapter.class);
        qlFragment.setQueuesAdapter(mockQueueAdapter);
        doNothing().when(mockQueueAdapter).updateQueueList(mockQueueList);
    }

    @Test
    public void testCreateNewQueue() {
        qlFragment.createNewQueue("Queue 1");
        qlFragment.createNewQueue("Queue 2");
        verify(mockQueueAdapter, times(2)).updateQueueList(qlFragment.getQueuesList());
        assertEquals(2, qlFragment.getQueuesList().size());
    }

    @Test
    public void testDeleteNewQueue(){
        qlFragment.createNewQueue("Queue 1");
        qlFragment.createNewQueue("Queue 2");
        qlFragment.removeWithPos(1);
        verify(mockQueueAdapter, times(2)).updateQueueList(qlFragment.getQueuesList());
        assertEquals(1, qlFragment.getQueuesList().size());
    }
}
