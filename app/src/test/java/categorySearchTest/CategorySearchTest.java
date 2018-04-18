package categorySearchTest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import junit.framework.Assert;

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
import static junit.framework.Assert.assertTrue;
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

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        csFrag = Mockito.mock(CategorySearchFragment.class);
        csFrag.setId("education");

        builder = new AlertDialog.Builder(activity);
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
        builder.setTitle("title");
        builder.setSingleChoiceItems(afFrag.getCategories(), 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                assertEquals("education",csFrag.getIdvar());
                activity.loadChildFragment(csFrag);
            }
        });

        AlertDialog alertDialog = builder.create();
        assertEquals(alertDialog, builder.show());
    }

}
