package randomPodcastTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.danoeh.antennapod.adapter.itunes.ItunesAdapter;
import de.danoeh.antennapod.fragment.RandomPodcastFragment;
import de.mfietz.fyydlin.SearchHit;

import static org.junit.Assert.*;

/**
 * Created by David on 2018-04-02.
 */
public class RandomPodcastFragmentTest {

    private RandomPodcastFragment testFrag;
    private List<ItunesAdapter.Podcast> testList;
    private Date date;
    private HashMap<Integer, Map<Integer, String>> map;

    @Mock
    private SearchHit testHit1, testHit2;

    @Before
    public void setUp(){
        testFrag = new RandomPodcastFragment();
        testList = new ArrayList<>();
        date = new Date();
        map = new HashMap<>();
    }

    @After
    public void tearDown(){
        testFrag = null;
        testList = null;
        date = null;
        map = null;
    }

    @Test
    public void getRandomTopic() throws Exception {
        String[] topics = {"Apple", "Orange", "Pear"};
        String randTopic;

        testFrag.setTopicList(topics);
        randTopic = testFrag.getRandomTopic();
        assertTrue(randTopic == topics[0] || randTopic == topics[1] || randTopic == topics[2]);
    }

    @Test
    public void getRandomPodcast() throws Exception {
        testHit1 = new SearchHit("Title 1", "", "", "","", "", "", "", map, "", date, 0);
        testHit2 = new SearchHit("Title 2", "", "", "","", "", "", "", map, "", date, 0);

        //Add podcasts to list
        testList.add(ItunesAdapter.Podcast.fromSearch(testHit1));
        testList.add(ItunesAdapter.Podcast.fromSearch(testHit2));

        assertEquals(2, testList.size());

        String randTitle = testFrag.getRandomPodcast(testList).title;
        assertTrue(randTitle == testList.get(0).title || randTitle == testList.get(1).title);
    }

    @Test
    public void getRandomNum() throws Exception {
        int min = 0;
        int max = 5;
        int randNum = testFrag.getRandomNum(min, max);
        assertTrue(randNum >= min && randNum <= max);
    }
}