package randomEpisodeTest;

import android.view.View;
import android.widget.Button;

import org.mockito.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import de.danoeh.antennapod.fragment.ItemlistFragment;

/**
 * Created by Vartan Benohanian on 2018-03-15.
 */

public class RandomEpisodeTest {

    @Test
    public void testRandomEpisode(){

        ItemlistFragment fragment = new ItemlistFragment();
        Button mockButton = Mockito.mock(Button.class);

        fragment.loadRandomEpisodeButton(mockButton);

        // test that an arbitrary OnClickListener object is set on the button
        verify(mockButton).setOnClickListener((View.OnClickListener) any());

    }
}