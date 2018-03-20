package staticThemeTest;

import org.junit.Test;
import static junit.framework.Assert.assertEquals;



import de.danoeh.antennapod.core.preferences.UserPreferences;

/*
 *Created by Anania on 15-03-2018
 */

public class testStaticTheme {


    @Test
    public void testThemeAssociations() {


        boolean allValid = false;


        int light = (de.danoeh.antennapod.core.R.style.Theme_AntennaPod_Light);
        int dark = (de.danoeh.antennapod.core.R.style.Theme_AntennaPod_Dark);
        int yellow = (de.danoeh.antennapod.core.R.style.Theme_AntennaPod_Yellow);
        int pink = (de.danoeh.antennapod.core.R.style.Theme_AntennaPod_Pink);

        int light2 = (de.danoeh.antennapod.core.R.style.Theme_AntennaPod_Light_NoTitle);
        int dark2 = (de.danoeh.antennapod.core.R.style.Theme_AntennaPod_Dark_NoTitle);
        int yellow2 = (de.danoeh.antennapod.core.R.style.Theme_AntennaPod_Yellow_NoTitle);
        int pink2 = (de.danoeh.antennapod.core.R.style.Theme_AntennaPod_Pink_NoTitle);

        if (light>0 && dark>0 && yellow>0 && pink>0 && light2>0 && dark2>0 && yellow2>0 && pink2>0){
            allValid=true;
        }

      assertEquals(allValid, true);





    }
}