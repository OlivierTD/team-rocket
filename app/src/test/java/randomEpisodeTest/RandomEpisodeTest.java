package randomEpisodeTest;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.danoeh.antennapod.core.feed.FeedItem;

import static junit.framework.Assert.assertTrue;

/**
 * Created by Vartan on 2018-03-15.
 */

public class RandomEpisodeTest {

    @Test
    public void testRandomEpisode(){

        FeedItem item1 = new FeedItem();
        FeedItem item2 = new FeedItem();
        FeedItem item3 = new FeedItem();

        List<FeedItem> itemList = new ArrayList<>();
        itemList.add(item1);
        itemList.add(item2);
        itemList.add(item3);

        while (itemList.get(0) == item1) {
            Collections.shuffle(itemList);
        }

        assertTrue("If the code reaches this line, then the test is a success.", true);

    }
}