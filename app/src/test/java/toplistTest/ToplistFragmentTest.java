package toplistTest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import static org.mockito.Mockito.doAnswer;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;

import de.danoeh.antennapod.fragment.ToplistFragment;

/**
 * Created by David on 2018-03-07.
 */

public class ToplistFragmentTest{

    //Mock values for onCreateView
    @Mock
    LayoutInflater mockInflater;
    @Mock
    ViewGroup mockViewGroup;
    @Mock
    Bundle mockBundle;

    private ToplistFragment testFragment;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testFragment = Mockito.mock(ToplistFragment.class);
    }

    @After
    public void tearDown() throws Exception {
        testFragment = null;
    }

    @Test
    public void testToplist(){
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                System.out.println("called with arguments: " + Arrays.toString(args));
                return null;
            }
        }).when(testFragment).onCreateView(mockInflater, mockViewGroup, mockBundle);
    }
}