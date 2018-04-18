package multipleQueueTests;

/**
 * Created by James on 2018-04-17.
 */

import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import de.danoeh.antennapod.core.feed.Queue;
import de.danoeh.antennapod.fragment.QueuesFragment;
import de.danoeh.antennapod.adapter.EpisodesAdapter;

import static junit.framework.Assert.assertEquals;

public class QueueFragmentTest {

    //class to be tested
    private QueuesFragment queuesFragment;
    //dependency
    private EpisodesAdapter mockEpisodesAdapter;




    @Before
    public void beforeTests(){
        MockitoAnnotations.initMocks(this);

        //Our fragment to be tested
        queuesFragment = new QueuesFragment();

        //Mocking dependencies
        mockEpisodesAdapter = mock(EpisodesAdapter.class);
        queuesFragment.setEpisodesAdapter(mockEpisodesAdapter);

    }

    @Test
    public void testRemoveEpisode(){
        Queue testQueue = new Queue("test");
        List<Long> episodeIdList = new ArrayList<>();
        ArrayList<Queue> queuesList = new ArrayList<>();
        episodeIdList.add((long) 1);
        episodeIdList.add((long) 2);
        testQueue.setEpisodesIDList(episodeIdList);
        queuesList.add(testQueue);
        queuesFragment.setQueue(testQueue);
        queuesFragment.setQueueList(queuesList);
        Long tester = (long) 2;
        Long tester2 = (long) 1;

        assertEquals(queuesFragment.getQueueList().get(0).equals(testQueue), true);
        assertEquals(queuesFragment.getQueue().getEpisodesIDList().get(0), tester2);
        assertEquals(queuesFragment.getQueueList().get(0).getEpisodesIDList().get(0), tester2);
        queuesFragment.removeQueue((long)1);
        assertEquals(queuesFragment.getQueue().getEpisodesIDList().get(0), tester);
        assertEquals(queuesFragment.getQueueList().get(0).getEpisodesIDList().get(0), tester);

    }


}
