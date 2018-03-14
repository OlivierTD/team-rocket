package mutipleQueueTests;

import org.junit.Test;

import de.danoeh.antennapod.fragment.QueueListFragment;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Created by James on 2018-03-09.
 */

public class testCreateNewQueues {


    @Test
    public void testCreateNewQueue(){
        QueueListFragment test = new QueueListFragment();
        test.createNewQueueTester();
        test.createNewQueueTester();
        assertEquals(2,test.getQueuesList().size());
    }
}
