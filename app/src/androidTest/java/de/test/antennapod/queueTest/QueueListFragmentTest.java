package de.test.antennapod.queueTest;

import junit.framework.TestCase;

import de.danoeh.antennapod.fragment.QueueListFragment;

/**
 * Created by James on 2018-03-08.
 */
public class QueueListFragmentTest extends TestCase {

    public void testCreateNewQueue() throws Exception {
        QueueListFragment test = new QueueListFragment();
        test.createNewQueue();
        test.createNewQueue();
        assertEquals(2,test.getQueuesList().size());

    }

}