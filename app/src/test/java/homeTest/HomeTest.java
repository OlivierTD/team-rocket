package homeTest;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import de.danoeh.antennapod.fragment.CategoriesFragment;
import de.danoeh.antennapod.fragment.FeaturedFragment;
import de.danoeh.antennapod.fragment.HomeFragment;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Sai Shan on 2018-04-14.
 */

public class HomeTest {


    @Mock
    private HomeFragment home;

    @Mock
    private HomeFragment.HomePagerAdapter homePagerAdapter;

    @Mock
    private CategoriesFragment categoriesFragment;

    @Mock
    private FragmentManager fragManager;

    @Mock
    private Resources res;
    
    @Mock
    FeaturedFragment ff = new FeaturedFragment();
    @Mock
    CategoriesFragment cf = new CategoriesFragment();
    @Mock
    private Fragment [] frag = {ff, cf,};

    @Before
    public void setUp(){
        home = new HomeFragment();
        homePagerAdapter = new HomeFragment.HomePagerAdapter(fragManager, res);
    }

    @After
    public void tearDown(){
        home = null;
        homePagerAdapter = null;
    }

    //testing the title of a given tab
    @Test
    public void getPageTitle (){
        CharSequence title = homePagerAdapter.getTitle(0);
        assertEquals("FEATURED",title);
    }

    //testing totalCount
    @Test
    public void getCount(){
        int  number = frag.length;
        assertEquals(2, number);
    }


    @Test
    public void getItem (){
        assertEquals(cf,frag[1]);
    }


}
