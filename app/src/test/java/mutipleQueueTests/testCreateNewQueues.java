package mutipleQueueTests;

import org.junit.Test;

import de.danoeh.antennapod.fragment.QueueListFragment;


import static junit.framework.Assert.assertEquals;


/**
 * Created by James on 2018-03-09.
 */

public class testCreateNewQueues {


    @Test
    public void testCreateNewQueue() {
        QueueListFragment test = new QueueListFragment();
        test.createNewQueueTester();
        test.createNewQueueTester();
        assertEquals(2, test.getQueuesList().size());
    }
}
