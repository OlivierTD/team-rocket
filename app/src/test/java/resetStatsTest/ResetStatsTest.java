package resetStatsTest;

import android.view.View;
import android.widget.Button;

import org.junit.Test;
import org.mockito.Mockito;

import de.danoeh.antennapod.fragment.StatisticsFragment;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import static junit.framework.Assert.assertEquals;

/**
 * Created by vartanbeno on 2018-04-18.
 */

public class ResetStatsTest {

    @Test
    public void testResetStatsButton() {

        StatisticsFragment statisticsFragment = new StatisticsFragment();
        Button mockButton = Mockito.mock(Button.class);

        statisticsFragment.setResetStatsButton(mockButton);
        statisticsFragment.setResetStatsFunctionality();

        // test that an arbitrary OnClickListener object is set on the button
        verify(mockButton).setOnClickListener((View.OnClickListener) any());

        assertEquals(statisticsFragment.getResetStatsButton(), mockButton);

    }

}
