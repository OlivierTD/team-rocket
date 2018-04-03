package statisticsFragmentTest;

import static org.mockito.Mockito.*;

import org.junit.Test;
import org.mockito.Mockito;

import de.danoeh.antennapod.adapter.StatisticsListAdapter;
import de.danoeh.antennapod.fragment.StatisticsFragment;

import static junit.framework.Assert.assertEquals;


/**
 * Created by Vartan on 2018-03-30.
 */

public class StatisticsFragmentTest {

    private StatisticsFragment statisticsFragment = new StatisticsFragment();

    private StatisticsListAdapter mockStatisticsListAdapter = Mockito.mock(StatisticsListAdapter.class);

    @Test
    public void testRefreshStatistics() {
        statisticsFragment.setListAdapter(mockStatisticsListAdapter);
        assertEquals(statisticsFragment.getListAdapter(), mockStatisticsListAdapter);
    }

}