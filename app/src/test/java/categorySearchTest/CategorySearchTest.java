package categorySearchTest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;

import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.fragment.AddFeedFragment;
import de.danoeh.antennapod.fragment.CategorySearchFragment;


import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Sai Shan on 2018-03-29.
 */

public class CategorySearchTest {


    @Mock
    private CategorySearchFragment csFrag;

    @Mock
    private AddFeedFragment afFrag;


    //mock alertdialog builder
    @Mock
    AlertDialog.Builder builder;

    //mock main activity
    @Mock
    MainActivity activity;

    //Mock values for onCreateView of the categorySearchFragment
    @Mock
    LayoutInflater mockInflater;
    @Mock
    ViewGroup mockViewGroup;
    @Mock
    Bundle mockBundle;

    @Mock
    AlertDialog alertDialog;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        csFrag = Mockito.mock(CategorySearchFragment.class);
        csFrag.setId("education");
    }

    @After
    public void tearDown() throws Exception {
        csFrag = null;
    }

    //mocking categories attribute
    @Test
    public void mockAttributeTest (){
        CharSequence [] ca = {"arts", "comedy","education"};
        afFrag = mock (AddFeedFragment.class);
        when(afFrag.getCategories()).thenReturn(ca);
        assertEquals(afFrag.getCategories(),ca);

    }

    //testing the creation of an alert dialog
    @Test
    public void builderTest() throws Exception{
        afFrag = mock(AddFeedFragment.class);
        builder = new AlertDialog.Builder(activity);
        builder.setTitle("title");
        builder.setSingleChoiceItems(afFrag.getCategories(), 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                csFrag.setId("education");
                activity.loadChildFragment(csFrag);
            }
        });

        builder.show();

    }

    //categorySearchFragment test
    @Test
    public void testCSFragment(){
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                System.out.println("called with arguments: " + Arrays.toString(args));
                return null;
            }
        }).when(csFrag).onCreateView(mockInflater, mockViewGroup, mockBundle);

    }



}
